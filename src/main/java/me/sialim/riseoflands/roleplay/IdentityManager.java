package me.sialim.riseoflands.roleplay;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.angeschossen.lands.api.player.LandPlayer;
import me.sialim.riseoflands.RiseOfLandsMain;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;

public class IdentityManager implements Listener, TabExecutor {
    RiseOfLandsMain plugin;
    LuckPerms lp;
    public final Map<UUID, IdentityData> playerDataMap = new HashMap<>();
    private File dataFile;
    private File usedNamesFile;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();
    private Set<String> usedNames = new HashSet<>();

    public IdentityManager(RiseOfLandsMain plugin) {
        this.plugin = plugin;
        this.lp = LuckPermsProvider.get();
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

    public float rerollMaxSize(UUID uuid) {
        float newMaxSize;
        if (isMale(uuid)) {
            newMaxSize = 0.9f + (float) (Math.random() * (1.15f - 0.9f));
        } else {

            newMaxSize = 0.9f + (float) (Math.random() * (0.95f - 0.85f));
        }
        return newMaxSize;
    }

    public void setSize(UUID uuid, float size) {
        Player p = Bukkit.getPlayer(uuid);
        p.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(size);
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
        if (dataFile == null) {
            return;
        }

        if (!plugin.getDataFolder().exists()) {
            dataFile.getParentFile().mkdirs();
            plugin.getDataFolder().mkdirs();
        }

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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (hasIdentity(player.getUniqueId())) return;
        player.setAllowFlight(true);
        player.setFlying(true);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }, 100L);
    }

    @EventHandler public void onPlayerMove(PlayerMoveEvent event) {
        promptIdentityCreation(event, event.getPlayer());
    }

    @EventHandler public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            promptIdentityCreation(e, p);
        } else if (e.getDamageSource() instanceof Player pe) {
            promptIdentityCreation(e, pe);
        }
    }

    private boolean hasValidIdentity(Player player) {
        UUID uuid = player.getUniqueId();
        IdentityData data = playerDataMap.get(uuid);
        return data != null && data.gender != null && data.roleplayName != null;
    }

    public LocalDate getBirthDate(UUID uuid) {
        return playerDataMap.get(uuid).getBirthDate();
    }

    private void promptIdentityCreation(Cancellable event, Player player) {
        if (!hasValidIdentity(player) && player.isOnline()) {
            player.sendMessage(ChatColor.YELLOW + "/identity create <gender> <first name> <middle initial> <last name> <suffix>");
            player.sendMessage(ChatColor.YELLOW + "NOTE: Keep it appropriate; You'll want to use a last name too.");
            //player.sendMessage(ChatColor.YELLOW + "Names cannot be reused.");
            event.setCancelled(event instanceof EntityDamageEvent);
            //event.setCancelled(true);
        }
    }

    private class IdentityData {
        private final UUID playerUUID;
        private Gender gender;
        private LabelSetting labelSetting;
        private String roleplayName;
        private DisplayMode displayMode;
        private final LocalDate birthDate;
        private final LocalDate deathDate;
        private float maxHeight;
        private boolean displayNation = true;

        public IdentityData(UUID playerUUID, Gender gender, LabelSetting labelSetting, String roleplayName, DisplayMode displayMode, LocalDate birthDate) {
            this.playerUUID = playerUUID;
            this.gender = gender;
            this.labelSetting = labelSetting;
            this.roleplayName = roleplayName;
            this.displayMode = displayMode;
            this.birthDate = birthDate;
            this.deathDate = calculateDeathDate(birthDate);
            generateHeight(gender);
        }

        private LocalDate calculateDeathDate(LocalDate birthDate) {
            int lifespan = 40 + (int) (Math.random() * 11);
            return birthDate.plusYears(lifespan);
        }

        private void generateHeight(Gender gender) {
            this.maxHeight = rerollMaxSize(playerUUID);
        }

        public void setLabelSetting(LabelSetting labelSetting) {
            this.labelSetting = labelSetting;
        }

        public void setDisplayMode(DisplayMode displayMode) {
            this.displayMode = displayMode;
        }

        public LocalDate getBirthDate() {
            return birthDate;
        }

        public LocalDate getDeathDate() {
            return deathDate;
        }

        public float getHeight() { return maxHeight; }

        public boolean isDisplayNation() {
            return displayNation;
        }

        public void setDisplayNation(boolean displayNation) {
            this.displayNation = displayNation;
        }
    }

    public float calculateSize(UUID uuid) {
        if (playerDataMap.containsKey(uuid)) {
            LocalDate birthDate = playerDataMap.get(uuid).getBirthDate();
            if (birthDate != null) {
                LocalDate currentDate = plugin.calendar.worldDates.get(plugin.getConfig().getString("main-world"));

                int currentAge = currentDate.getYear() - birthDate.getYear();
                int maxAge = 18;
                float minSize = 0.7f;

                currentAge = Math.min(currentAge, maxAge);

                double size = (playerDataMap.get(uuid).getHeight() - minSize) * Math.sqrt((1.0 / maxAge) * currentAge) + minSize;
                if (size < 0.7) {
                    size = 0.7;
                }
                return (float) size;
            }
        }
        return 1f;
    }

    public String calculateAge(UUID uuid) {
        LocalDate birthDate = playerDataMap.get(uuid).getBirthDate();
        LocalDate currentDate = plugin.calendar.worldDates.get(plugin.getConfig().getString("main-world"));


        if (birthDate.equals(currentDate)) {
            return "0 days";
        }

        int years = currentDate.getYear() - birthDate.getYear();
        int days = currentDate.getDayOfYear() - birthDate.getDayOfYear();

        if (birthDate.getDayOfYear() > currentDate.getDayOfYear()) {
            years--;
            days += birthDate.lengthOfYear();
        }

        String yearText = years == 1 ? "1 year" : years + " years";
        String dayText = days == 1 ? "1 day" : days + " days";

        if (years > 0) {
            return yearText + " " + ((days > 0) ? dayText : "");
        } else {
            return days > 0 ? dayText : "Born today";
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

    public IdentityData createIdentity(UUID playerUUID, Gender gender, LabelSetting labelSetting, String roleplayName, DisplayMode displayMode, LocalDate currentDate) {
        IdentityData identity = new IdentityData(playerUUID, gender, labelSetting, roleplayName, displayMode, currentDate);
        playerDataMap.put(playerUUID, identity);
        return identity;
    }

    public IdentityData getIdentity(UUID playerUUID) {
        return playerDataMap.get(playerUUID);
    }

    public boolean hasIdentity(UUID playerUUID) {
        return playerDataMap.containsKey(playerUUID);
    }

    public String getRoleplayName(UUID uuid) {
        IdentityData data = playerDataMap.get(uuid);
        return (data != null && data.roleplayName != null) ? data.roleplayName : Bukkit.getPlayer(uuid).getName();
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
            if (args.length < 3) {
                p.sendMessage(ChatColor.RED + "Usage: /identity create <gender> <first name> <middle initial> <last name> <suffix>");
                return false;
            }

            Gender gender;
            try {
                gender = Gender.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                p.sendMessage(ChatColor.RED + "Invalid gender. Please use 'male' or 'female'.");
                return false;
            }

            String roleplayName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            if (!roleplayName.matches("[A-Za-z .,'\"öäüáóàòìíéèêëîâôû-]+") || roleplayName.length() > 24){
                p.sendMessage(ChatColor.RED + "Invalid roleplay name. The name must be alphanumeric and less than 24 characters.");
                return false;
            }

            if (playerDataMap.containsKey(uuid)) {
                p.sendMessage(ChatColor.RED + "You already have a gender and roleplay name set.");
                return false;
            }

            LocalDate currentDate = plugin.calendar.worldDates.getOrDefault(plugin.getConfig().getString("main-world"), LocalDate.of(476, 1, 1));
            LocalDate birthDate = currentDate.minusYears(16);
            playerDataMap.put(uuid, new IdentityData(p.getUniqueId(), gender, LabelSetting.FIXED, roleplayName, DisplayMode.RANK, birthDate));
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

        if (args[0].equalsIgnoreCase("shownation") && args.length == 2) {
            if (!playerDataMap.containsKey(uuid)) {
                p.sendMessage(ChatColor.RED + "You need to create a gender and roleplay name first.");
                return false;
            }

            boolean displayMode;
            try {
                displayMode = Boolean.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                p.sendMessage(ChatColor.RED + "Invalid display mode. Please use 'true' or 'false'.");
                return false;
            }

            IdentityData data = playerDataMap.get(uuid);
            data.setDisplayNation(displayMode);
            playerDataMap.put(uuid, data);
            p.sendMessage(ChatColor.GREEN + "Your show-nation label mode has been updated to " + String.valueOf(displayMode) + ".");
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
                return getStaffLabel(getPlayerStaffRank(player), data, getPlayerRank(player));
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

        List<String> rankOrder = Arrays.asList("supporter", "knight", "lord", "baron", "duke", "prince", "king", "peasant");

        for (String rank : rankOrder) {
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
        //User user = lp.getUserManager().getUser(player.getUniqueId());

        List<String> staffRankOrder = Arrays.asList("builder", "helper", "mod", "god", "dev", "admin");

        for (String staffRank : staffRankOrder) {
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
        return false;
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

    public String getStaffLabel(String staffRank, IdentityData identityData, String playerRank) {
        if (staffRank == null || identityData == null) return "Unknown0";

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
        String label = plugin.getConfig().getString(path, null);

        if (label == null) {
            label = playerRank;
        }

        return ChatColor.translateAlternateColorCodes('&', label);
    }

    public boolean isShowingNation(IdentityData identityData) {
        if (identityData == null) return false;
        return identityData.displayNation;
    }

    public String getLandLabel(Player p) {
        LandPlayer lP = plugin.api.getLandPlayer(p.getUniqueId());
        if (lP == null || lP.getEditLand(false) == null) {
            return ChatColor.GRAY + "None";
        }
        return plugin.api.getLandPlayer(p.getUniqueId()).getEditLand().getColorName();
    }

    public boolean isMale(UUID uuid) {
        return getGender(uuid) == Gender.MALE;
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;

        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("create");
            suggestions.add("label");
            suggestions.add("displaylabel");
            suggestions.add("shownation");
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

            if (args[0].equalsIgnoreCase("shownation")) {
                suggestions.add("true");
                suggestions.add("false");
            }
        } else if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("create")) {
                suggestions.add("<roleplay name>");
            }
        }

        return suggestions;
    }
}
