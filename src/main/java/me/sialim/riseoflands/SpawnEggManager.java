package me.sialim.riseoflands;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.*;

public class SpawnEggManager implements Listener, CommandExecutor {
    private RiseOfLandsMain plugin;
    private NamespacedKey spawnerKey;
    private int CHECK_RADIUS = 5;
    private long CHECK_INTERVAL = 60L;

    public SpawnEggManager(RiseOfLandsMain plugin) {
        this.plugin = plugin;
        this.spawnerKey = new NamespacedKey(plugin, "spawner_usable");
    }

    public ItemStack createSpawnEgg(EntityType entityType, boolean canUseOnSpawner) {
        ItemStack egg = new ItemStack(Material.valueOf(entityType.name() + "_SPAWN_EGG"));
        ItemMeta meta = egg.getItemMeta();

        if (meta != null) {
            meta.getPersistentDataContainer().set(spawnerKey, PersistentDataType.BOOLEAN, canUseOnSpawner);
            meta.setDisplayName((canUseOnSpawner ? "Spawner-Compatible " : "Non-Spawner ") + entityType.name() + " Egg");
            egg.setItemMeta(meta);
        }

        return egg;
    }

    @EventHandler
    public void onRightClickSpawner(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getItem() == null) return;

        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();

        if (block.getType() == Material.SPAWNER && item.getType().name().endsWith("_SPAWN_EGG")) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(spawnerKey, PersistentDataType.BOOLEAN)) {
                boolean canUseOnSpawner = meta.getPersistentDataContainer().get(spawnerKey, PersistentDataType.BOOLEAN);
                if (!canUseOnSpawner) {
                    event.setCancelled(true);
                    //event.getPlayer().sendMessage(ChatColor.RED + "This spawn egg cannot be used on spawners.");
                }
            }
        }
    }

    @EventHandler
    public void onPlaceSpawner(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.SPAWNER) return;

        ItemStack item = event.getItemInHand();
        if (!item.getType().name().endsWith("_SPAWN_EGG")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(spawnerKey, PersistentDataType.BOOLEAN)) {
            boolean canUseOnSpawner = meta.getPersistentDataContainer().get(spawnerKey, PersistentDataType.BOOLEAN);

            if (canUseOnSpawner) {
                BlockState state = block.getState();
                if (state instanceof CreatureSpawner spawner) {
                    spawner.setSpawnedType(EntityType.valueOf(item.getType().name().replace("_SPAWN_EGG", "")));
                    spawner.update();
                }
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /givespawnegg <entity> <allowedOnSpawners>");
            return true;
        }

        String entityName = args[0].toUpperCase();
        boolean allowedOnSpawners = Boolean.parseBoolean(args[1]);

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(entityName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid entity type!");
            return true;
        }

        Material spawnEggMaterial = Material.getMaterial(entityType.name() + "_SPAWN_EGG");
        if (spawnEggMaterial == null) {
            sender.sendMessage(ChatColor.RED + "That entity does not have a spawn egg!");
            return true;
        }

        ItemStack spawnEgg = new ItemStack(spawnEggMaterial);
        ItemMeta meta = spawnEgg.getItemMeta();
        if (meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(spawnerKey, PersistentDataType.BOOLEAN, allowedOnSpawners);
            //meta.setDisplayName(ChatColor.RED + entityType.name().replace("_", " ") + " Spawn Egg");
            List<String> lore = new ArrayList<>();
            if (!allowedOnSpawners) {
                lore.add(ChatColor.RED + "This egg is incompatible with spawners.");
            }
            meta.setLore(lore);
            spawnEgg.setItemMeta(meta);
        }

        Bukkit.getPlayer(sender.getName()).getInventory().addItem(spawnEgg);
        sender.sendMessage(ChatColor.GREEN + "Given a " + entityType.name().replace("_", " ") + " spawn egg!");

        return true;
    }

    public void startSpawnerProximityCheck() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location playerLocation = player.getLocation();
                for (int x = -CHECK_RADIUS; x <= CHECK_RADIUS; x++) {
                    for (int y = -CHECK_RADIUS; y <= CHECK_RADIUS; y++) {
                        for (int z = -CHECK_RADIUS; z <= CHECK_RADIUS; z++) {
                            Block block = playerLocation.clone().add(x, y, z).getBlock();
                            if (block.getType() == Material.SPAWNER) {
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    BlockState state = block.getState();
                                    if (state instanceof CreatureSpawner) {
                                        CreatureSpawner spawner = (CreatureSpawner) state;
                                        if (spawner.getSpawnedType() == EntityType.VILLAGER) {
                                            spawner.setSpawnedType(EntityType.ZOMBIE);
                                            spawner.update();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }, 0L, CHECK_INTERVAL);
    }
}
