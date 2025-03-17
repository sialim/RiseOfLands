package me.sialim.riseoflands.lands;

import me.sialim.riseoflands.RiseOfLandsMain;
import me.sialim.riseoflands.SpawnEggManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SpawnManager implements Listener, TabExecutor {
    private Map<UUID, Location> previousLocations = new HashMap<>();
    private Map<UUID, Long> cooldowns = new HashMap<>();
    private RiseOfLandsMain plugin;
    private Location spawnLocation;

    public SpawnManager(RiseOfLandsMain plugin) {
        this.plugin = plugin;
        this.spawnLocation = Bukkit.getWorld(plugin.getConfig().getString("main-world")).getSpawnLocation();
    }

    public boolean hasPreviousLocation(Player p) {
        return previousLocations.containsKey(p.getUniqueId());
    }

    public Location getPreviousLocation(Player p) {
        return previousLocations.getOrDefault(p.getUniqueId(), p.getLocation());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) return false;

        if (command.getName().equals("spawn")) {
            if (args.length == 0) {
                if (previousLocations.containsKey(p.getUniqueId())) {
                    p.sendMessage("You are already in spawn! Use /spawn leave to return to your previous location.");
                    return true;
                }

                teleportWithStandStill(p, spawnLocation, 3);
                return true;
            } else if (args.length == 1 && args[0].equals("leave")) {
                if (previousLocations.containsKey(p.getUniqueId())) {
                    Location previousLocation = previousLocations.get(p.getUniqueId());
                    teleportWithStandStill(p, previousLocation, 3);

                    previousLocations.remove(p.getUniqueId());
                } else {
                    p.sendMessage("You are not currently in spawn.");
                }
                return true;
            }
        }
        return false;
    }

    public void teleportWithStandStill(Player p, Location destination, int standStillTime) {
        if (destination.equals(spawnLocation)) {
            previousLocations.put(p.getUniqueId(), p.getLocation());
        }

        p.sendMessage("Teleporting in " + standStillTime + " seconds. Remain still or it will be cancelled.");

        new BukkitRunnable() {
            int timer = standStillTime;
            Location initialLocation = p.getLocation();

            @Override public void run() {
                if (p.getLocation().distance(initialLocation) > 0.1) {
                    p.sendMessage("Movement detected; Teleportation cancelled.");
                    this.cancel();
                    return;
                }

                if (timer <= 0) {
                    p.teleport(destination);
                    p.sendMessage("Teleportation completed.");
                    this.cancel();
                } else {
                    p.playSound((Entity) p, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    timer--;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args){
        if (args.length == 1) {
            return Arrays.asList("leave");
        }
        return null;
    }
}