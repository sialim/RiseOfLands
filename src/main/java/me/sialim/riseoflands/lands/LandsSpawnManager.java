package me.sialim.riseoflands.lands;

import me.angeschossen.lands.api.events.land.spawn.LandSpawnTeleportEvent;
import me.sialim.riseoflands.RiseOfLandsMain;
import me.sialim.riseoflands.utilities.RMath;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LandsSpawnManager implements Listener {
    RiseOfLandsMain plugin;
    private Economy economy;
    private Map<UUID, PendingTeleport> pendingTeleports = new HashMap<>();
    public LandsSpawnManager (RiseOfLandsMain plugin) {
        this.plugin = plugin;
    }

    private Economy getEconomy() {
        if (economy == null) {
            economy = plugin.econ;
        }
        return economy;
    }

    @EventHandler
    public void onLandSpawn(LandSpawnTeleportEvent e) {
        Player p = e.getLandPlayer().getPlayer();
        UUID uuid = e.getPlayerUUID();
        Location playerLocation = p.getLocation();
        Location targetLocation = e.getLand().getSpawn();
        double teleportCost = RMath.calculateDistanceTax(playerLocation, targetLocation);

        if (playerLocation.distance(targetLocation) <= 1000) return;

        Economy economy = getEconomy();
        if (economy == null) {
            p.sendMessage(ChatColor.RED + "Economy system is not available. Please contact an administrator.");
            return;
        }
        if (pendingTeleports.containsKey(uuid)) {
            PendingTeleport pending = pendingTeleports.get(uuid);

            if (pending.getTargetLocation().equals(targetLocation) &&
                    pending.getPlayerLastLocation().equals(playerLocation) &&
                    pending.getPlayerLastLocation().getWorld().equals(playerLocation.getWorld())) {
                if (economy.getBalance(p) < teleportCost) {
                    p.sendMessage(ChatColor.RED + "You need $" + String.format("%.2f", teleportCost) + " to teleport. You currently have: " + String.format("%.2f", economy.getBalance(p)));
                } else {
                    economy.withdrawPlayer(p, teleportCost);
                    p.teleport(targetLocation);
                    p.sendMessage(ChatColor.GREEN + "Teleported to land spawn for $" + String.format("%.2f", teleportCost) + ".");
                }
                pendingTeleports.remove(uuid);
                e.setCancelled(false);
                return;
            } else {
                pendingTeleports.remove(uuid);
            }
        }

        pendingTeleports.put(uuid, new PendingTeleport(playerLocation, targetLocation));
        e.setCancelled(true);

        p.sendMessage(ChatColor.YELLOW + "Teleporting to land spawn costs $" + String.format("%.2f", teleportCost) + ".");
        p.sendMessage(ChatColor.GREEN + "Run the command again to confirm teleportation.");
    }

    private String formatTimeRemaining(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        return String.format("%d minutes %d seconds", minutes, seconds);
    }

    private static class PendingTeleport {
        private final Location playerLastLocation;
        private final Location targetLocation;

        public PendingTeleport(Location playerLastLocation, Location targetLocation) {
            this.playerLastLocation = playerLastLocation;
            this.targetLocation = targetLocation;
        }

        public Location getPlayerLastLocation() {
            return playerLastLocation;
        }

        public Location getTargetLocation() {
            return targetLocation;
        }
    }

}
