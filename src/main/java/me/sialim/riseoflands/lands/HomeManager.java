package me.sialim.riseoflands.lands;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.sialim.riseoflands.RiseOfLandsMain;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class HomeManager implements CommandExecutor {
    private File homesFile;
    private Gson gson = new Gson();
    private Map<UUID, Map<String, Location>> playerHomes;
    private Economy economy;
    RiseOfLandsMain plugin;

    public HomeManager(RiseOfLandsMain plugin) {
        this.plugin = plugin;
        this.playerHomes = new HashMap<>();
        this.homesFile = new File(plugin.getDataFolder(), "homes.json");
        loadHomes();
    }

    public void loadHomes() {
        if (!homesFile.exists()) return;

        try (FileReader reader = new FileReader(homesFile)) {
            Type type = new TypeToken<Map<UUID, Map<String, Location>>>() {}.getType();
            playerHomes = gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveHomes() {
        try (FileWriter writer = new FileWriter(homesFile)) {
            gson.toJson(playerHomes, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setHome(Player player, String homeName) {
        UUID uuid = player.getUniqueId();
        int maxHomes = getMaxHomes(player);

        if (playerHomes.containsKey(uuid) && playerHomes.get(uuid).size() >= maxHomes) {
            player.sendMessage("You cannot create any more homes.");
            return;
        }

        if (!playerHomes.containsKey(uuid)) {
            playerHomes.put(uuid, new HashMap<>());
        }

        playerHomes.get(uuid).put(homeName, player.getLocation());
        player.sendMessage("Home '" + homeName + "' has been set.");
        saveHomes();
    }

    public void teleportToHome(Player player, String homeName) {
        UUID uuid = player.getUniqueId();
        if (playerHomes.containsKey(uuid)) {
            Map<String, Location> homes = playerHomes.get(uuid);
            if (homes.containsKey(homeName)) {
                //player.teleport(homes.get(homeName));
                plugin.sm.teleportWithStandStill(player, homes.get(homeName), 3);
                player.sendMessage("Teleported to home '" + homeName + "'.");
            } else {
                player.sendMessage("You don't have a home named '" + homeName + "'.");
            }
        } else {
            player.sendMessage("You have no homes set.");
        }
    }

    public void listHomes(Player p) {
        UUID uuid = p.getUniqueId();
        if (playerHomes.containsKey(uuid)) {
            p.sendMessage("List of your homes: ");
            for (String homeName : playerHomes.get(uuid).keySet()) {
                p.sendMessage("- " + homeName);
            }
        } else {
            p.sendMessage("You have no homes set yet.");
        }
    }

    private int getMaxHomes(Player player) {
        for (int i = 10; i >= 1; i--) {
            if (player.hasPermission("riseoflands.homes." + i)) {
                return i;
            }
        }
        return 1;
    }

    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            return false;
        }

        if (command.getName().equals("home")) {
            if (args.length == 0) {
                listHomes(p);
            } else if (args.length == 1) {
                teleportToHome(p, args[0]);
                return true;
            }
        } else if (command.getName().equals("sethome")) {
            if (args.length == 0) {
                setHome(p, "default");
                return true;
            } else if (args.length == 1) {
                setHome(p, args[0]);
                return true;
            }
        }
        return false;
    }
}
