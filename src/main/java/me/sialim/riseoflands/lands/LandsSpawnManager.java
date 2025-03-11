package me.sialim.riseoflands.lands;

import me.angeschossen.lands.api.events.land.spawn.LandSpawnTeleportEvent;
import me.sialim.riseoflands.RiseOfLandsMain;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LandsSpawnManager implements Listener {
    RiseOfLandsMain plugin;
    private Map<UUID, Long> cooldowns = new HashMap<>();
    private final long cooldownTime = 30 * 1000;
    private int teleportCost = 100;
    public LandsSpawnManager (RiseOfLandsMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler public void onLandSpawn(LandSpawnTeleportEvent e) {
        Player p = e.getLandPlayer().getPlayer();
        UUID uuid = e.getPlayerUUID();

        if (cooldowns.containsKey(uuid)) {
            long timeLeft = cooldownTime - (System.currentTimeMillis() - cooldowns.get(uuid));
            if (timeLeft > 0) {
                p.sendMessage("You need to wait " + formatTimeRemaining(timeLeft) + " before teleporting to a land spawn.");
                e.setCancelled(true);
                return;
            }
        }

        if (plugin.getEconomy().getBalance(p) < teleportCost) {
            p.sendMessage("You need at least $" + teleportCost + " to teleport to a land spawn.");
            e.setCancelled(true);
            return;
        }

        e.setCancelled(false);
        plugin.getEconomy().withdrawPlayer(p, teleportCost);
        cooldowns.put(uuid, System.currentTimeMillis());
        p.sendMessage("Teleported to land spawn for $" + teleportCost + ".");
    }

    private String formatTimeRemaining(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        return String.format("%d minutes %d seconds", minutes, seconds);
    }
}
