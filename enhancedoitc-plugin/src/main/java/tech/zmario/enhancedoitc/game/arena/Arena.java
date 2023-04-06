package tech.zmario.enhancedoitc.game.arena;

import com.grinderwolf.swm.api.world.SlimeWorld;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import tech.zmario.enhancedoitc.common.enums.GameState;
import tech.zmario.enhancedoitc.common.objects.Placeholder;
import tech.zmario.enhancedoitc.common.objects.User;
import tech.zmario.enhancedoitc.common.redis.packets.impl.GameConnectPacket;
import tech.zmario.enhancedoitc.common.redis.packets.impl.GameDisconnectPacket;
import tech.zmario.enhancedoitc.common.redis.packets.impl.GameUpdatePacket;
import tech.zmario.enhancedoitc.common.utils.Utils;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.enums.MessagesConfiguration;
import tech.zmario.enhancedoitc.game.enums.SettingsConfiguration;
import tech.zmario.enhancedoitc.game.objects.ArenaConfig;
import tech.zmario.enhancedoitc.game.objects.Kit;
import tech.zmario.enhancedoitc.game.objects.PlayerCache;
import tech.zmario.enhancedoitc.game.tasks.GameStartingTask;
import tech.zmario.enhancedoitc.game.utils.InventorySerializer;
import tech.zmario.enhancedoitc.game.utils.LocationUtils;
import tech.zmario.enhancedoitc.game.utils.WorldUtils;
import tech.zmario.enhancedoitc.game.workload.impl.TeleportablePlayerWorkload;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
@Setter
public class Arena {

    private final EnhancedOITC plugin;
    private final String name, displayName;
    private final List<UUID> players = new ArrayList<>();
    private final List<Location> spawnLocations = new ArrayList<>();
    private final Map<UUID, PlayerCache> playerCaches = new HashMap<>();
    private final ArenaConfig arenaConfig;
    private int id;
    private SlimeWorld slimeWorld;

    private int minPlayers, maxPlayers;
    private Location lobbyLocation;
    private GameState gameState = GameState.RESTARTING;

    private GameStartingTask gameStartingTask;

    private Kit kit;

    public Arena(EnhancedOITC plugin, String name) {
        this.plugin = plugin;
        this.name = name;

        this.arenaConfig = new ArenaConfig(plugin, name);
        this.displayName = arenaConfig.getConfig().getString("display-name");

        this.id = plugin.getArenaManager().getNextArenaId();

        this.kit = InventorySerializer.deserialize(arenaConfig.getConfig().getString("kit-items"),
                arenaConfig.getConfig().getString("kit-armor"));

        if ((slimeWorld = WorldUtils.loadWorld(plugin,
                LocationUtils.deserializeLocationToDouble(arenaConfig.getConfig().getString("lobby-location")),
                name, true)) == null) {
            load(Bukkit.getWorld(name));
            return;
        }

        plugin.getArenaManager().addInQueue(this);
    }

    public void load(World world) {
        plugin.getArenaManager().removeFromQueue(this);

        this.minPlayers = arenaConfig.getConfig().getInt("min-players");
        this.maxPlayers = arenaConfig.getConfig().getInt("max-players");

        this.lobbyLocation = LocationUtils.deserializeLocation(arenaConfig.getConfig().getString("lobby-location"), world);

        for (String spawnLocation : arenaConfig.getConfig().getStringList("spawn-locations")) {
            spawnLocations.add(LocationUtils.deserializeLocation(spawnLocation, world));
        }

        gameState = GameState.WAITING;
        gameStartingTask = new GameStartingTask(this);

        plugin.getArenaManager().getArenas().put(name, this);

        sendConnectPacket();

        plugin.debug("Loaded arena " + name + " and sent connect packet");
    }

    public void addPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        if (isPlayer(player)) return;

        plugin.debug("Adding player " + player.getName() + " to arena " + name);

        players.add(uuid);
        playerCaches.put(uuid, new PlayerCache(player.getUniqueId(), player.getName()));

        if (players.size() >= minPlayers && gameState == GameState.WAITING) {
            setGameState(GameState.STARTING);
        }

        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        MessagesConfiguration.JOIN_MESSAGE.broadcast(this, plugin,
                new Placeholder("player", player.getName()),
                new Placeholder("players", players.size() + ""),
                new Placeholder("max-players", maxPlayers + ""));

        plugin.getWorkloadThread().addWorkload(new TeleportablePlayerWorkload(player, lobbyLocation));
        GameUpdatePacket updatePacket = new GameUpdatePacket(SettingsConfiguration.SERVER_NAME.getString(plugin),
                name, gameState, players.size());

        plugin.getRedisHandler().publish(updatePacket.getChannel(), updatePacket);
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();

        players.remove(uuid);
        plugin.getArenaManager().getArenasByPlayer().remove(player.getUniqueId());

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        if (players.isEmpty() && gameState == GameState.PLAYING) {
            setGameState(GameState.RESTARTING);
        }

        if (gameState == GameState.STARTING || gameState == GameState.WAITING) {
            MessagesConfiguration.LEAVE_MESSAGE.broadcast(this, plugin,
                    new Placeholder("player", player.getName()),
                    new Placeholder("players", players.size() + ""),
                    new Placeholder("max-players", maxPlayers + ""));
        }

        if (gameState == GameState.PLAYING)
            checkGameEnd(null);

        if (player.isOnline())
            Utils.sendPlayersToServer(plugin, SettingsConfiguration.LOBBY_SERVER.getString(plugin),
                Collections.singletonList(player));
    }

    public boolean isPlayer(Player player) {
        return players.contains(player.getUniqueId());
    }

    public void handleDeath(Player damaged, Player damager) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        PlayerCache damagedCache = getCache(damaged);
        PlayerCache damagerCache = getCache(damager);

        damagedCache.addDeath();
        damagerCache.addKill();

        damaged.setHealth(20);
        damaged.setFoodLevel(20);
        damaged.setFireTicks(0);
        damaged.setFallDistance(0);

        damaged.closeInventory();

        MessagesConfiguration.PLAYER_DEATH.broadcast(this, plugin,
                new Placeholder("player", damaged.getName()),
                new Placeholder("killer", damager.getName()));

        plugin.getWorkloadThread().addWorkload(new TeleportablePlayerWorkload(damaged,
                spawnLocations.get(random.nextInt(spawnLocations.size()))));

        plugin.getStorage().getUser(damaged.getUniqueId()).addDeath();
        plugin.getStorage().getUser(damager.getUniqueId()).addKill();

        checkGameEnd(damagerCache);
    }

    public PlayerCache getCache(Player player) {
        return playerCaches.get(player.getUniqueId());
    }

    public void checkGameEnd(@Nullable PlayerCache selected) {
        if (selected == null) {
            selected = playerCaches.values().stream()
                    .filter(playerCache -> playerCache.getKills() > 0)
                    .max(Comparator.comparingInt(PlayerCache::getKills))
                    .orElse(null);
        }

        if (selected == null || selected.getKills() < SettingsConfiguration.GAME_WIN_KILLS.getInt(plugin)) return;

        List<PlayerCache> topKillers = playerCaches.values().stream()
                .filter(cache -> cache.getKills() > 0)
                .sorted(Comparator.comparingInt(PlayerCache::getKills))
                .limit(3)
                .collect(Collectors.toList());

        Collections.reverse(topKillers);

        PlayerCache firstCache = topKillers.size() > 0 ? topKillers.get(0) : null;
        PlayerCache secondCache = topKillers.size() > 1 ? topKillers.get(1) : null;
        PlayerCache thirdCache = topKillers.size() > 2 ? topKillers.get(2) : null;
        String none = MessagesConfiguration.PLACEHOLDER_NONE.getString(plugin, null);

        Placeholder firstName = new Placeholder("first-name", (firstCache == null ? none : firstCache.getName()));
        Placeholder firstKills = new Placeholder("first-kills", (firstCache == null ? "0" : firstCache.getKills() + ""));
        Placeholder secondName = new Placeholder("second-name", (secondCache == null ? none : secondCache.getName()));
        Placeholder secondKills = new Placeholder("second-kills", (secondCache == null ? "0" : secondCache.getKills() + ""));
        Placeholder thirdName = new Placeholder("third-name", (thirdCache == null ? none : thirdCache.getName()));
        Placeholder thirdKills = new Placeholder("third-kills", (thirdCache == null ? "0" : thirdCache.getKills() + ""));

        for (PlayerCache cache : playerCaches.values()) {
            User user = plugin.getStorage().getUser(cache.getUuid());

            if (cache.equals(firstCache)) {
                user.addWin();
            } else {
                user.addLoss();
            }

            plugin.getRedisHandler().updateUser(user);
        }

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            MessagesConfiguration.GAME_END.send(player, plugin, firstName, firstKills, secondName, secondKills, thirdName, thirdKills);
            MessagesConfiguration.TITLE_GAME_END.sendTitle(player, plugin, firstName, firstKills, secondName, secondKills, thirdName, thirdKills);
            MessagesConfiguration.SOUND_GAME_END.playSound(player, plugin);
        }

        restart();
    }

    public void restart() {
        sendDisconnectPacket();

        setGameState(GameState.RESTARTING);

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            Utils.sendPlayersToServer(plugin, SettingsConfiguration.LOBBY_SERVER.getString(plugin),
                            players.stream().map(Bukkit::getPlayer).collect(Collectors.toList()))
                    .thenAccept(v -> {
                        playerCaches.clear();
                        players.clear();
                        lobbyLocation.getWorld().getEntities().stream()
                                .filter(entity -> !(entity instanceof Player))
                                .forEach(Entity::remove);
                    });

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.unloadWorld(lobbyLocation.getWorld(), false);
                slimeWorld = WorldUtils.loadWorld(plugin,
                        LocationUtils.deserializeLocationToDouble(arenaConfig.getConfig().getString("lobby-location")),
                        name, true);

                plugin.getArenaManager().addInQueue(this);

                id = plugin.getArenaManager().getNextArenaId();
            }, 30L);
        }, SettingsConfiguration.GAME_RESTART_COUNTDOWN.getInt(plugin) * 20L);

        gameStartingTask = null;
    }

    public void sendConnectPacket() {
        GameConnectPacket connectPacket = new GameConnectPacket(SettingsConfiguration.SERVER_NAME.getString(plugin),
                name, maxPlayers);

        plugin.getRedisHandler().publish(connectPacket.getChannel(), connectPacket);
    }

    public void sendDisconnectPacket() {
        GameDisconnectPacket disconnectPacket = new GameDisconnectPacket(SettingsConfiguration.SERVER_NAME.getString(plugin), name);

        plugin.getRedisHandler().publish(disconnectPacket.getChannel(), disconnectPacket);
    }
}