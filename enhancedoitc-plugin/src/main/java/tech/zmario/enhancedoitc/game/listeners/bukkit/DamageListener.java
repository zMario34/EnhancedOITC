package tech.zmario.enhancedoitc.game.listeners.bukkit;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import tech.zmario.enhancedoitc.common.enums.GameState;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;
import tech.zmario.enhancedoitc.game.enums.MessagesConfiguration;

import java.util.Optional;

@RequiredArgsConstructor
public class DamageListener implements Listener {

    private final EnhancedOITC plugin;

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        boolean isProjectile = event.getDamager() instanceof Projectile;
        if ((!(event.getDamager() instanceof Player) && !isProjectile)
                || !(event.getEntity() instanceof Player)) return;
        Entity damagerEntity = event.getDamager();
        Player damaged = (Player) event.getEntity();
        Player damager = null;

        if (damagerEntity instanceof Player) {
            damager = (Player) damagerEntity;
        } else {
            if (!isProjectile) return;
            Projectile projectile = (Projectile) damagerEntity;

            if (projectile.getShooter() instanceof Player)
                damager = (Player) projectile.getShooter();
        }

        if (damager == null || damagerEntity.getUniqueId().equals(damaged.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        Optional<Arena> damagerArenaOptional = plugin.getArenaManager().getArena(damager.getUniqueId());
        Optional<Arena> damagedArenaOptional = plugin.getArenaManager().getArena(damaged.getUniqueId());

        if (damagedArenaOptional.isEmpty() || damagerArenaOptional.isEmpty()) return;
        Arena damagerArena = damagerArenaOptional.get();
        Arena damagedArena = damagedArenaOptional.get();

        if (!damagerArena.equals(damagedArena) ||
                damagerArena.getGameState() != GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        if (isProjectile || damaged.getHealth() - event.getFinalDamage() <= 0.0D) {
            event.setDamage(0.0D);
            damagedArena.handleDeath(damaged, damager);

            MessagesConfiguration.SOUND_PLAYER_KILL.playSound(damager, plugin);
            MessagesConfiguration.SOUND_PLAYER_DEATH.playSound(damaged, plugin);

            if (!damager.getInventory().contains(Material.ARROW))
                damager.getInventory().addItem(new ItemStack(Material.ARROW));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Optional<Arena> arenaOptional = plugin.getArenaManager().getArena(player.getUniqueId());

        if (arenaOptional.isEmpty()) return;
        Arena arena = arenaOptional.get();

        if (arena.getGameState() != GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }
}
