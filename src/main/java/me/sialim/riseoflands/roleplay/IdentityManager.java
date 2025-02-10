package me.sialim.riseoflands.roleplay;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import me.sialim.riseoflands.RiseOfLands;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;

public class IdentityManager implements Listener, TabExecutor {
    RiseOfLands plugin;
    private final Map<UUID, IdentityData> playerDataMap = new HashMap<>();
    private File dataFile;
    private File usedNamesFile;
    private final Gson gson = new Gson();
    private Set<String> usedNames = new HashSet<>();

    public IdentityManager(RiseOfLands plugin) {
        this.plugin = plugin;
    }

    public void initializeDataFile() {
        dataFile = new File(plugin.getDataFolder(), "identityData.json");
        usedNamesFile = new File(plugin.getDataFolder(), "usedNames.txt");
        loadUsedNames();
        loadPlayerData();
    }

    private void loadUsedNames() {
        try {
            if (!usedNamesFile.exists()) {
                usedNamesFile.getParentFile().mkdirs();
                usedNamesFile.createNewFile();
            }
            if (usedNamesFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(usedNamesFile))) {
                    String line = reader.readLine();
                    if (line != null) {
                        String[] names = line.split(",");
                        usedNames.addAll(Arrays.asList(names));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUsedNames() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(usedNamesFile))) {
            writer.write(String.join(",", usedNames));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPlayerData() {
        try {
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            }
            try (Reader reader = new FileReader(dataFile)) {
                Type type = new TypeToken<Map<UUID, IdentityData>>() {}.getType();
                Map<UUID, IdentityData> data = gson.fromJson(reader, type);
                if (data != null) playerDataMap.putAll(data);
            } catch (IOException e) {
                e.printStackTrace();;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void savePlayerData() {
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(playerDataMap, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();
        playerDataMap.remove(uuid);
        savePlayerData();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!hasValidIdentity(player)) {
            player.sendMessage(ChatColor.YELLOW + "/identity create <gender> <first name>, <middle initial>, <last name>, <suffix>");
            player.sendMessage(ChatColor.YELLOW + "NOTE: Keep it appropriate; You'll want to use a last name too.");
            player.sendMessage(ChatColor.YELLOW + "Names cannot be reused.");
            event.setCancelled(true);
        }
    }

    private boolean hasValidIdentity(Player player) {
        UUID uuid = player.getUniqueId();
        IdentityData data = playerDataMap.get(uuid);
        return data != null && data.gender != null && data.roleplayName != null;
    }

    private static class IdentityData {
        private Gender gender;
        private LabelSetting labelSetting;
        private String roleplayName;

        public IdentityData(Gender gender, LabelSetting labelSetting, String roleplayName) {
            this.gender = gender;
            this.labelSetting = labelSetting;
            this.roleplayName = roleplayName;
        }

        public void setLabelSetting(LabelSetting labelSetting) {
            this.labelSetting = labelSetting;
        }
    }

    private String formatRoleplayName(String name) {
        if (name == null || name.trim().isEmpty()) return "";

        String[] words = name.split(" ");
        StringBuilder formattedName = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            if (word.length() > 1) {
                word = Character.toUpperCase(word.charAt(0)) + word.substring(1);
            } else {
                word = word.toUpperCase();
            }
            if (i == words.length - 1 && word.matches("(?i)(jr|sr|ii|iii|iv|v|vi|vii|viii|ix|x)")) {
                word = word.toUpperCase();
            }

            formattedName.append(word).append(" ");
        }

        return formattedName.toString().trim();
    }

    public String getRoleplayName(UUID uuid) {
        IdentityData data = playerDataMap.get(uuid);
        return (data != null && data.roleplayName != null) ? formatRoleplayName(data.roleplayName) : "Unknown";
    }

    public Gender getGender(UUID uuid) {
        IdentityData data = playerDataMap.get(uuid);
        return (data != null && data.gender != null) ? data.gender : null;
    }

    public String getGenderDisplay(UUID uuid) {
        IdentityData data = playerDataMap.get(uuid);
        return (data != null && data.gender != null) ? capitalizeFirstLetter(data.gender.name()) : "Unknown";
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public LabelSetting getLabelSetting(UUID uuid) {
        IdentityData data = playerDataMap.get(uuid);
        return (data != null) ? data.labelSetting : null;
    }

    private enum Gender {
        MALE, FEMALE
    }

    private enum LabelSetting {
        FIXED, MALE, FEMALE
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Command can only be executed by players.");
            return false;
        }

        UUID uuid = p.getUniqueId();

        if (args.length < 2) {
            p.sendMessage(ChatColor.RED + "Usage: /create <create|label> ...");
            return false;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                p.sendMessage(ChatColor.RED + "Usage: /identity create <gender> <first name>, <middle initial>, <last name>, <suffix>");
                return false;
            }

            Gender gender = null;
            try {
                gender = Gender.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                p.sendMessage(ChatColor.RED + "Invalid gender. Please use 'male' or 'female'.");
                return false;
            }

            String roleplayName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            if (playerDataMap.containsKey(uuid)) {
                p.sendMessage(ChatColor.RED + "You already have a gender and roleplay name set.");
                return false;
            }

            playerDataMap.put(uuid, new IdentityData(gender, LabelSetting.FIXED, roleplayName));
            p.sendMessage(ChatColor.GREEN + "Your gender has been set to: " + gender.name() +".");
            p.sendMessage("Welcome to the world " + roleplayName);
            savePlayerData();
            return true;
        }

        if (args[0].equalsIgnoreCase("label") && args.length == 2) {
            if (!playerDataMap.containsKey(uuid)) {
                p.sendMessage(ChatColor.RED + "You need to create a gender and roleplay name first.");
                return false;
            }

            LabelSetting labelSetting = LabelSetting.FIXED;
            try {
                labelSetting = LabelSetting.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                p.sendMessage(ChatColor.RED + "Invalid label. Please use 'fixed', 'male', or 'female'.");
                return false;
            }

            IdentityData data = playerDataMap.get(uuid);
            data.setLabelSetting(labelSetting);
            playerDataMap.put(uuid, data);
            p.sendMessage(ChatColor.GREEN + "Your label setting has been updated to " + labelSetting.name() + ".");
            savePlayerData();
            return true;
        }

        return false;
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;

        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("create");
            suggestions.add("label");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                suggestions.add("male");
                suggestions.add("female");
            }

            if (args[0].equalsIgnoreCase("label")) {
                suggestions.add("fixed");
                suggestions.add("male");
                suggestions.add("female");
            }
        } else if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("create")) {
                suggestions.add("<roleplay name");
            }
        }

        return suggestions;
    }
}
