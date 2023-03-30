package tech.zmario.enhancedoitc.game.listeners.game;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import tech.zmario.enhancedoitc.game.EnhancedOITC;
import tech.zmario.enhancedoitc.game.arena.Arena;
import tech.zmario.enhancedoitc.common.enums.GameState;

import java.util.Optional;

@RequiredArgsConstructor
public class DamageListener implements Listener {

    private final EnhancedOITC plugin;

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile) ||
                !(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        Entity damagerEntity = event.getDamager();
        Player damager = null;

        if (damagerEntity instanceof Player) {
            damager = (Player) damagerEntity;
        } else {
            Projectile projectile = (Projectile) damagerEntity;

            if (projectile.getShooter() instanceof Player)
                damager = (Player) projectile.getShooter();
        }

        if (damager == null) {
            event.setCancelled(true);
            return;
        }
        Player damaged = (Player) event.getEntity();

        Optional<Arena> damagerArenaOptional = plugin.getArenaManager().getArena(damager.getUniqueId());
        Optional<Arena> damagedArenaOptional = plugin.getArenaManager().getArena(damaged.getUniqueId());

        if (damagedArenaOptional.isEmpty() || damagerArenaOptional.isEmpty()) return;
        Arena damagerArena = damagerArenaOptional.get();
        Arena damagedArena = damagedArenaOptional.get();

        if (!damagerArena.equals(damagedArena) ||
                damagerArena.getGameState() == GameState.WAITING ||
                damagerArena.getGameState() == GameState.STARTING ||
                damagerArena.isSpectator(damager)) {
            event.setCancelled(true);
            return;
        }

        if (event.getDamager() instanceof Projectile ||
                damaged.getHealth() - event.getFinalDamage() <= 0.0D) {
            event.setDamage(0.0D);
            damagedArena.handleDeath(damaged, damager);
        }
    }
}
