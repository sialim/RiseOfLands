package me.sialim.riseoflands.roleplay;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import me.angeschossen.lands.api.player.LandPlayer;
import me.sialim.riseoflands.RiseOfLands;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
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
    public final Map<UUID, IdentityData> playerDataMap = new HashMap<>();
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
                Type type = new TypeToken<Map<String, IdentityData>>() {}.getType();
                Map<String, IdentityData> data = gson.fromJson(reader, type);
                if (data != null) {
                    data.forEach((uuidStr, identityData) -> playerDataMap.put(UUID.fromString(uuidStr), identityData));
                }
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
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        playerDataMap.remove(uuid);
        savePlayerData();
    }

    @EventHandler public void onPlayerMove(PlayerMoveEvent event) {
        promptIdentityCreation(event, event.getPlayer());
    }

    @EventHandler public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            promptIdentityCreation(e, p);
        }
    }

    private boolean hasValidIdentity(Player player) {
        UUID uuid = player.getUniqueId();
        IdentityData data = playerDataMap.get(uuid);
        return data != null && data.gender != null && data.roleplayName != null;
    }

    private void promptIdentityCreation(Cancellable event, Player player) {
        if (!hasValidIdentity(player)) {
            player.sendMessage(ChatColor.YELLOW + "/identity create <gender> <first name> <middle initial> <last name> <suffix>");
            player.sendMessage(ChatColor.YELLOW + "NOTE: Keep it appropriate; You'll want to use a last name too.");
            player.sendMessage(ChatColor.YELLOW + "Names cannot be reused.");
            event.setCancelled(true);
        }
    }

    private static class IdentityData {
        private Gender gender;
        private LabelSetting labelSetting;
        private String roleplayName;
        private DisplayMode displayMode;

        public IdentityData(Gender gender, LabelSetting labelSetting, String roleplayName, DisplayMode displayMode) {
            this.gender = gender;
            this.labelSetting = labelSetting;
            this.roleplayName = roleplayName;
            this.displayMode = displayMode;
        }

        public void setLabelSetting(LabelSetting labelSetting) {
            this.labelSetting = labelSetting;
        }

        public void setDisplayMode(DisplayMode displayMode) {
            this.displayMode = displayMode;
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

    private enum DisplayMode {
        RANK, STAFF, GENDER, LAND
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
                p.sendMessage(ChatColor.RED + "Usage: /identity create <gender> <first name> <middle initial> <last name> <suffix>");
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

            playerDataMap.put(uuid, new IdentityData(gender, LabelSetting.FIXED, roleplayName, DisplayMode.RANK));
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

            LabelSetting labelSetting;
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

        if (args[0].equalsIgnoreCase("displaylabel") && args.length == 2) {
            if (!playerDataMap.containsKey(uuid)) {
                p.sendMessage(ChatColor.RED + "You need to create a gender and roleplay name first.");
                return false;
            }

            DisplayMode displayMode;
            try {
                displayMode = DisplayMode.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                p.sendMessage(ChatColor.RED + "Invalid display mode. Please use 'rank', 'staff', 'land', or 'gender'.");
                return false;
            }

            IdentityData data = playerDataMap.get(uuid);
            data.setDisplayMode(displayMode);
            playerDataMap.put(uuid, data);
            p.sendMessage(ChatColor.GREEN + "Your display label mode has been updated to " + displayMode.name() + ".");
            savePlayerData();
            return true;
        }

        return false;
    }

    public String getDisplayLabel(Player player) {
        UUID uuid = player.getUniqueId();
        IdentityData data = playerDataMap.get(uuid);
        if (data == null) return "Unknown";

        switch (data.displayMode) {
            case RANK:
                return getRankLabel(getPlayerRank(player), data);
            case STAFF:
                return getStaffLabel(getPlayerStaffRank(player), data);
            case GENDER:
                return getGenderDisplay(uuid);
            case LAND:
                return getLandLabel(player);
            default:
                return "Unknown";
        }
    }

    public int getPurchasableRankWeight(String rank) {
        switch (rank.toLowerCase()) {
            case "supporter":
                return 0;
            case "knight":
                return 1;
            case "lord":
                return 2;
            case "baron":
                return 3;
            case "duke":
                return 4;
            case "prince":
                return 5;
            case "king":
                return 6;
            case "peasant":
            case "default":
            default:
                return -1;
        }
    }

    public String getPlayerRank(Player player) {
        String highestRank = "default";
        int highestWeight = getPurchasableRankWeight(highestRank);

        for (String rank : Arrays.asList("supporter", "knight", "lord", "baron", "duke", "prince", "king", "peasant")) {
            if (player.hasPermission("group." + rank)) {
                int weight = getPurchasableRankWeight(rank);
                if (weight > highestWeight) {
                    highestRank = rank;
                    highestWeight = weight;
                }
            }
        }

        return highestRank;
    }

    public String getPlayerStaffRank(Player player) {
        String highestStaffRank = null;
        int highestWeight = 0;

        for (String staffRank : Arrays.asList("builder", "helper", "mod", "god", "dev", "admin")) {
            if (player.hasPermission("group." + staffRank)) {
                int weight = getStaffRankWeight(staffRank);
                if (weight > highestWeight) {
                    highestStaffRank = staffRank;
                    highestWeight = weight;
                }
            }
        }

        if (highestStaffRank == null) return getPlayerRank(player);

        return highestStaffRank;
    }

    public int getStaffRankWeight(String rank) {
        switch (rank.toLowerCase()) {
            case "god":
                return 10;
            case "dev":
                return 9;
            case "admin":
                return 8;
            case "mod":
                return 7;
            case "helper":
                return 6;
            case "builder":
                return 5;
            default:
                return 0;
        }
    }

    public boolean hasArtisan(Player player) {
        return player.hasPermission("group.artisan");
    }

    public String getRankLabel(String rank, IdentityData identityData) {
        if (rank == null || identityData == null) return "Unknown";

        String genderString;
        switch (identityData.labelSetting) {
            case FIXED:
                genderString = (identityData.gender == Gender.MALE) ? "male" : "female";
                break;
            case MALE:
                genderString = "male";
                break;
            case FEMALE:
                genderString = "female";
                break;
            default:
                genderString = "Unknown";
        }

        String path = "ranks." + rank.toLowerCase().replace(" ", "_") + "." + genderString;
        String label = plugin.getConfig().getString(path, "Unknown");

        if (label.equals("Unknown")) {
            path = "ranks.default." + genderString;
            label = plugin.getConfig().getString(path, "Unknown");
        }

        return ChatColor.translateAlternateColorCodes('&', label);
    }

    public String getStaffLabel(String staffRank, IdentityData identityData) {
        if (staffRank == null || identityData == null) return "Unknown";

        String genderString;
        switch (identityData.labelSetting) {
            case FIXED:
                genderString = (identityData.gender == Gender.MALE) ? "male" : "female";
                break;
            case MALE:
                genderString = "male";
                break;
            case FEMALE:
                genderString = "female";
                break;
            default:
                genderString = "Unknown";
        }

        String path = "staff." + staffRank.toLowerCase().replace(" ", "_") + "." + genderString;
        String label = plugin.getConfig().getString(path, "Unknown");

        if (label.equals("Unknown")) {
            path = "staff.default." + genderString;
            label = plugin.getConfig().getString(path, "Unknown");
        }

        return ChatColor.translateAlternateColorCodes('&', label);
    }

    public String getLandLabel(Player p) {
        LandPlayer lP = plugin.api.getLandPlayer(p.getUniqueId());
        if (lP == null || lP.getEditLand() == null) {
            return ChatColor.GRAY + "None";
        }
        return plugin.api.getLandPlayer(p.getUniqueId()).getEditLand().getColorName();
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;

        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("create");
            suggestions.add("label");
            suggestions.add("displaylabel");
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

            if (args[0].equalsIgnoreCase("displaylabel")) {
                suggestions.add("rank");
                suggestions.add("staff");
                suggestions.add("land");
                suggestions.add("gender");
            }
        } else if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("create")) {
                suggestions.add("<roleplay name>");
            }
        }

        return suggestions;
    }
}
