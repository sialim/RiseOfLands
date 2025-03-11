package me.sialim.riseoflands.culture.trait_events;

import me.sialim.riseoflands.RiseOfLandsMain;
import me.sialim.riseoflands.culture.RTrait;
import me.sialim.riseoflands.culture.traits.PacifismMobCTrait;
import me.sialim.riseoflands.culture.traits.PacifismPlayerCTrait;
import me.sialim.riseoflands.culture.traits.PacifismPlayerMobCTrait;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PacifismListener implements Listener {
    public RiseOfLandsMain plugin;

    private final Map<UUID, UUID> combatTracker = new ConcurrentHashMap<>();

    public PacifismListener(RiseOfLandsMain plugin) { this.plugin = plugin; }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity target = e.getEntity();

        if (target instanceof Player player && isHostileMob(damager)) {
            trackCombat(player.getUniqueId(), damager.getUniqueId());
        }

        if (damager instanceof Player attacker) {
            handlePlayerDamage(attacker, target);
        }

        else if (damager instanceof Projectile projectile) {
            handleProjectileDamage(projectile, target);
        }
    }

    private void handlePlayerDamage(Player attacker, Entity target) {
        UUID attackerUUID = attacker.getUniqueId();

        if (combatTracker.containsKey(attackerUUID) && combatTracker.get(attackerUUID).equals(target.getUniqueId())) {
            return;
        }

        if (target instanceof Player && hasTrait(attackerUUID, new PacifismPlayerCTrait())) {
            checkForPacifism(attackerUUID, new PacifismPlayerCTrait());
        } else if (isHostileMob(target) && hasTrait(attackerUUID, new PacifismMobCTrait())) {
            checkForPacifism(attackerUUID, new PacifismMobCTrait());
        } else if ((target instanceof Player || isHostileMob(target)) &&
                hasTrait(attackerUUID, new PacifismPlayerMobCTrait())) {
            checkForPacifism(attackerUUID, new PacifismPlayerMobCTrait());
        }
    }

    private void handleProjectileDamage(Projectile projectile, Entity target) {
        if (projectile.getShooter() instanceof Player shooter) {
            UUID shooterUUID = shooter.getUniqueId();

            if (combatTracker.containsKey(shooterUUID) && combatTracker.get(shooterUUID).equals(target.getUniqueId())) {
                return;
            }

            if (target instanceof Player && hasTrait(shooterUUID, new PacifismPlayerCTrait())) {
                checkForPacifism(shooterUUID, new PacifismPlayerCTrait());
            } else if (isHostileMob(target) && hasTrait(shooterUUID, new PacifismMobCTrait())) {
                checkForPacifism(shooterUUID, new PacifismMobCTrait());
            } else if ((target instanceof Player || isHostileMob(target)) &&
                    hasTrait(shooterUUID, new PacifismPlayerMobCTrait())) {
                checkForPacifism(shooterUUID, new PacifismPlayerMobCTrait());
            }
        }
    }


    public boolean hasTrait(UUID uuid, RTrait pacifTrait) {
        if (plugin.religionManager.getPlayerCulture(uuid) == null) {
            return false;
        }
        return plugin.religionManager.getPlayerCulture(uuid).getTraits()
                .stream().anyMatch(trait -> trait.getName().equals(pacifTrait.getName()));
    }
    private void checkForPacifism(UUID uuid, RTrait trait) {
        plugin.religionManager.handleTraitViolation(uuid, trait, trait.getPoints());
    }

    private boolean isHostileMob(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        EntityType type = entity.getType();
        return PacifismMobCTrait.restricted.contains(type);
    }

    private void trackCombat(UUID targetUUID, UUID attackerUUID) {
        if (combatTracker.containsKey(targetUUID) && combatTracker.get(targetUUID).equals(attackerUUID))
            return;
        combatTracker.put(targetUUID, attackerUUID);
        Bukkit.getScheduler().runTaskLater(plugin, () -> combatTracker.remove(targetUUID), 200L);
    }
}
