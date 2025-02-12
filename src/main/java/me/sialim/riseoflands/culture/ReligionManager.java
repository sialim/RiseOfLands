package me.sialim.riseoflands.culture;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import me.sialim.riseoflands.RiseOfLands;
import me.sialim.riseoflands.culture.trait_events.SilenceListener;
import me.sialim.riseoflands.culture.traits.*;
import me.sialim.riseoflands.government.ReputationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ReligionManager {
    public RiseOfLands plugin;
    private final File religionFile;
    private final File cooldownFile;
    private final Map<String, Religion> religions;
    public Map<UUID, ReligionCooldown> cooldowns;
    private final ReputationManager reputationManager;

    public ReligionManager(RiseOfLands plugin, ReputationManager reputationManager) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.reputationManager = reputationManager;
        this.religionFile = new File(plugin.getDataFolder(), "cultures.json");
        this.cooldownFile = new File(plugin.getDataFolder(), "cooldowns.json");
        this.religions = new HashMap<>();
        this.cooldowns = new HashMap<>();

        // File creation
        if (religionFile.exists()) {
            loadCulturesFromJson();
        } else {
            try {
                religionFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create cultures.json file: " + e.getMessage());
            }
        }
        if (cooldownFile.exists()) {
            loadCooldownsFromFile();
        } else {
            try {
                cooldownFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create cooldowns.json file: " + e.getMessage());
            }
        }
    }

    public void loadCulturesFromJson() {
        // Ensure the religion file exists and is not empty
        if (!religionFile.exists() || religionFile.length() == 0) {
            // Log that the file is empty or doesn't exist
            Bukkit.getLogger().info("The cultures JSON file is empty or does not exist. No cultures loaded.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(religionFile))) {
            // Create Gson instance with the RTraitAdapter registered
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(RTrait.class, new RTraitAdapter())  // Register RTraitAdapter for RTrait deserialization
                    .create();

            JsonElement culturesJson = JsonParser.parseReader(reader);

            if (culturesJson.isJsonArray()) {
                JsonArray culturesArray = culturesJson.getAsJsonArray();

                for (JsonElement cultureElement : culturesArray) {
                    // Deserialize the culture using the Gson with RTraitAdapter
                    Religion culture = gson.fromJson(cultureElement, Religion.class);
                    religions.put(culture.getName(), culture);  // Store the culture by name
                }
            } else {
                // Log if the JSON is not in an array (unexpected format)
                Bukkit.getLogger().warning("The cultures JSON file is not in the expected array format.");
            }
        } catch (IOException | JsonParseException e) {
            // Handle errors during reading or parsing
            Bukkit.getLogger().severe("Error loading cultures from file: " + e.getMessage());
        }
    }

    public void loadCooldownsFromFile() {
        // Check if the cooldown file exists and is not empty
        if (!cooldownFile.exists() || cooldownFile.length() == 0) {
            // Log that the file is empty or doesn't exist
            Bukkit.getLogger().info("The cooldowns JSON file is empty or does not exist. No cooldowns loaded.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(cooldownFile))) {
            JsonElement jsonElement = JsonParser.parseReader(reader);

            // Ensure the JSON is an object (map-like structure for cooldowns)
            if (jsonElement.isJsonObject()) {
                JsonObject cooldownsObject = jsonElement.getAsJsonObject();

                // Create a Gson instance and register necessary adapters
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(RTrait.class, new RTraitAdapter()) // Register RTrait adapter
                        .registerTypeAdapter(ReligionCooldown.class, new ReligionCooldownAdapter()) // Register ReligionCooldown adapter
                        .create();

                // Deserialize cooldown data into the cooldowns map
                for (Map.Entry<String, JsonElement> entry : cooldownsObject.entrySet()) {
                    UUID playerUUID = UUID.fromString(entry.getKey());
                    ReligionCooldown cooldown = gson.fromJson(entry.getValue(), ReligionCooldown.class);
                    cooldowns.put(playerUUID, cooldown);
                }
            } else {
                // Log if the JSON is not an object (unexpected format)
                Bukkit.getLogger().warning("The cooldowns JSON file is not in the expected object format.");
            }
        } catch (IOException | JsonParseException e) {
            // Handle errors during reading or parsing
            Bukkit.getLogger().severe("Error loading cooldowns from file: " + e.getMessage());
        }
    }

    public void saveCulturesToJson() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(religionFile))) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(RTrait.class, new RTraitAdapter())  // Register RTraitAdapter
                    .create();

            // Create an array to hold all cultures
            JsonArray culturesArray = new JsonArray();

            for (Religion culture : religions.values()) {
                // Convert the traits in the religion to maps
                List<Map<String, Object>> traitsMap = new ArrayList<>();
                for (RTrait trait : culture.getTraits()) {
                    traitsMap.add(trait.toMap());
                }

                // Create a Map for the culture data
                Map<String, Object> cultureData = new HashMap<>();
                cultureData.put("name", culture.getName());
                cultureData.put("owner", culture.getOwner().toString());
                cultureData.put("members", culture.getMembers().stream().map(UUID::toString).collect(Collectors.toList()));
                cultureData.put("traits", traitsMap);  // Add traits data

                // Serialize to JSON
                JsonElement cultureJson = gson.toJsonTree(cultureData);
                culturesArray.add(cultureJson);  // Add the serialized culture to the array
            }

            // Write the entire cultures array to file
            gson.toJson(culturesArray, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCooldownsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cooldownFile))) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ReligionCooldown.class, new ReligionCooldownAdapter())  // Register the custom adapter for ReligionCooldown
                    .create();

            String json = gson.toJson(cooldowns);

            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String createCulture(UUID playerName, String cultureName) {
        for (Religion culture : religions.values()) {
            if (culture.getMembers().contains(playerName))
                return "You are already part of a religion and cannot create a new one.";
        }

        if (religions.containsKey(cultureName))
            return "Religion already exists.";


        ReligionCooldown cooldown = cooldowns.getOrDefault(playerName, new ReligionCooldown());
        long currentTime = System.currentTimeMillis();

        if (currentTime - cooldown.getCultureDeleteTime() < TimeUnit.DAYS.toMillis(2))
            return "You cannot create a religion for another " +
                    ((TimeUnit.DAYS.toMillis(2) - (currentTime - cooldown.getCultureDeleteTime())) / 1000 / 60 / 60) +
                    " hours.";

        cooldown.setReputationReset(false);
        cooldown.setCultureCreateTime(System.currentTimeMillis());
        cooldown.setCultureJoinTime(System.currentTimeMillis());
        cooldowns.put(playerName, cooldown);
        saveCooldownsToFile();

        // Remove 5 rep points
        int currentRep = reputationManager.getPlayerReputation(playerName);
        reputationManager.setPlayerReputation(playerName, currentRep - 5);

        List<RTrait> sampleTraits = Arrays.asList(
                new TrueCarnivoreCTrait(),
                new HostileMobCTrait(EntityType.SLIME),
                new MagicCTrait(),
                new SilenceCTrait(),
                new PacifismPlayerMobCTrait()
        );

        String playerUsername = Bukkit.getPlayer(playerName).getName();

        religions.put(cultureName, new Religion(cultureName, playerName, Collections.singletonList(playerName), sampleTraits));
        saveCulturesToJson();
        return "Religion, " + cultureName + ", created by " + playerUsername + ".";
    }

    public String joinCulture(UUID playerName, String cultureName) {
        Religion culture = religions.get(cultureName);
        if (culture == null)
            return "Religion does not exist.";

        if (isPlayerInAnyCulture(playerName))
            return "You are already in a religion.";

        ReligionCooldown cooldown = cooldowns.getOrDefault(playerName, new ReligionCooldown());

        if (System.currentTimeMillis() - cooldown.getCultureLeaveTime() < TimeUnit.DAYS.toMillis(2)) {
            return "You cannot join another religion for another " +
                    ((TimeUnit.DAYS.toMillis(2) - (System.currentTimeMillis() - cooldown.getCultureLeaveTime())) / 1000 / 60 / 60) +
                    " hours.";
        }

        cooldown.setReputationReset(false);
        cooldown.setCultureJoinTime(System.currentTimeMillis());
        cooldowns.put(playerName, cooldown);
        saveCooldownsToFile();

        culture.addMember(playerName);
        saveCulturesToJson();
        return playerName + " joined the religion " + cultureName + ".";
    }

    public String leaveCulture(UUID playerName) {
        for (Religion culture : religions.values()) {
            if (culture.getMembers().contains(playerName)) {
                if (culture.getOwner().equals(playerName))
                    return "You cannot leave your religion because you are the owner. Consider deleting it instead.";

                ReligionCooldown cooldown = cooldowns.getOrDefault(playerName, new ReligionCooldown());

                if (System.currentTimeMillis() - cooldown.getCultureJoinTime() < TimeUnit.DAYS.toMillis(1)) {
                    return "You cannot leave the religion until " +
                            ((TimeUnit.DAYS.toMillis(1) - (System.currentTimeMillis() - cooldown.getCultureJoinTime())) / 1000 / 60 / 60) +
                            " hours.";
                }

                cooldown.setCultureLeaveTime(System.currentTimeMillis());
                cooldowns.put(playerName, cooldown);
                saveCooldownsToFile();

                culture.removeMember(playerName);
                saveCulturesToJson();
                return playerName + " left the religion " + culture.getName() + ".";
            }
        }
        return "Player is not in any religion.";
    }

    private boolean isPlayerInAnyCulture(UUID playerName) {
        for (Religion culture : religions.values()) {
            if (culture.getMembers().contains(playerName)) {
                return true;
            }
        }
        return false;
    }

    public Religion getPlayerCulture(UUID playerName) {
        for (Religion culture : getCultureNames().stream().map(this::getCulture).toList()) {
            if (culture.getMembers().contains(playerName))
                return getCulture(culture.getName());
        }
        return null;
    }

    public Religion getCulture(String cultureName) {
        return religions.get(cultureName);
    }

    public String deleteCulture(UUID playerName, Religion cultureName) {
        if (cultureName == null)
            return "Religion does not exist.";

        if (!cultureName.getOwner().equals(playerName))
            return "You are not the owner of this religion.";

        ReligionCooldown cooldown = cooldowns.getOrDefault(playerName, new ReligionCooldown());
        cooldown.setCultureDeleteTime(System.currentTimeMillis());
        cooldown.setCultureLeaveTime(System.currentTimeMillis());
        cooldowns.put(playerName, cooldown);
        saveCooldownsToFile();

        for (UUID player : cultureName.getMembers()) {
            ReligionCooldown playerCooldown = cooldowns.getOrDefault(player, new ReligionCooldown());
            playerCooldown.setCultureLeaveTime(System.currentTimeMillis());
            cooldowns.put(player, playerCooldown);
        }

        // Remove 5 rep points
        int currentRep = reputationManager.getPlayerReputation(playerName);
        reputationManager.setPlayerReputation(playerName, currentRep - 5);

        religions.remove(cultureName);
        saveCulturesToJson();
        return "Culture " + cultureName + " has been deleted.";
    }

    public void resetReputation(UUID playerName) {
        ReligionCooldown cooldown = cooldowns.getOrDefault(playerName, new ReligionCooldown());
        long currentTime = System.currentTimeMillis();

        if (cooldown.getReputationReset()) return;

        if (currentTime - cooldown.getCultureDeleteTime() >= TimeUnit.DAYS.toMillis(2)) {
            if (cooldown.getCultureDeleteTime() > 0) {
                reputationManager.setPlayerReputation(playerName, 5);
                cooldown.setReputationReset(true);
                cooldowns.put(playerName, cooldown);
                saveCooldownsToFile();
                String username = Bukkit.getPlayer(playerName).getName();
                Bukkit.getLogger().info(username + " has had their reputation reset after deleting their religion.");
            }
        }
    }

    public String forceForgive(UUID playerName) {
        if (getPlayerCulture(playerName) == null) return "Player is not in culture.";
        ReligionCooldown cooldown = cooldowns.getOrDefault(playerName, new ReligionCooldown());
        long currentTime = System.currentTimeMillis();
        Player p = Bukkit.getPlayer(playerName);

        int newRep = clampReputation(getPlayerCulture(playerName), 50);
        reputationManager.setPlayerReputation(playerName, newRep);
        cooldown.setReputationReset(true);
        cooldown.clearBrokenTraits();
        cooldowns.put(playerName, cooldown);
        saveCooldownsToFile();
        String username = Bukkit.getPlayer(playerName).getName();
        int rep = reputationManager.getPlayerReputation(playerName);
        p.sendMessage("Your sins have been forgiven. You now have " + rep + " reputation points.");
        Bukkit.getLogger().info(username + " has been forgiven of their sins. New rep: " + rep);

        return p.getName() + " has been forgiven. They now have " + rep + " reputation points.";
    }

    public void forgive(UUID playerName) {
        ReligionCooldown cooldown = cooldowns.getOrDefault(playerName, new ReligionCooldown());
        long currentTime = System.currentTimeMillis();

        if ((currentTime - cooldown.getForgivenessTimer() >= TimeUnit.DAYS.toMillis(2))
        && currentTime - cooldown.getCultureJoinTime() >= TimeUnit.DAYS.toMillis(1)) {
            if (cooldown.getForgivenessTimer() > 0) {
                int newRep = clampReputation(getPlayerCulture(playerName), 50);
                reputationManager.setPlayerReputation(playerName, newRep);
                cooldown.setReputationReset(true);
                cooldown.clearBrokenTraits();
                cooldowns.put(playerName, cooldown);
                cooldown.setForgivenessTimer(0);
                saveCooldownsToFile();
                String username = Bukkit.getPlayer(playerName).getName();
                Player p = Bukkit.getPlayer(playerName);
                int rep = reputationManager.getPlayerReputation(playerName);
                p.sendMessage("Your sins have been forgiven. You now have " + rep + " reputation points.");
                Bukkit.getLogger().info(username + " has been forgiven of their sins. New rep: " + rep);
            }
        }
    }

    public int getTraitPoints(Religion religion) {
        int traitPoints = 0;
        for (RTrait trait : religion.getTraits()) {
            traitPoints += trait.getPoints();
        }
        return traitPoints;
    }


    public List<String> getCultureNames() {
        return new ArrayList<>(religions.keySet());
    }

    public void handleTraitViolation(UUID uuid, RTrait trait, int points) {
        ReligionManager rm = plugin.religionManager;
        ReputationManager rpm = plugin.reputationManager;
        ReligionCooldown cooldown = rm.cooldowns.getOrDefault(uuid, new ReligionCooldown());
        long currentTime = System.currentTimeMillis();
        cooldown.setForgivenessTimer(currentTime);

        Set<RTrait> brokenTraits = cooldown.getBrokenTraits();
        if (brokenTraits == null) {
            brokenTraits = new HashSet<>();
            cooldown.setBrokenTraits(brokenTraits);
        }
        if (!brokenTraits.contains(trait)) {
            brokenTraits.add(trait);
            int currentRep = rpm.getPlayerReputation(uuid);
            int newRep = currentRep - points;
            int setRep = clampReputation(getPlayerCulture(uuid), newRep);
            rpm.setPlayerReputation(uuid, setRep);
            currentRep = rpm.getPlayerReputation(uuid);
            Bukkit.getPlayer(uuid).sendMessage("You violated a " + trait.getName() +
                    " tradition! You lost " + points + " reputation. You now have " +
                    currentRep + " reputation points.");
            rm.saveCooldownsToFile();
        }
        rm.cooldowns.put(uuid, cooldown);
    }

    public int clampReputation(Religion religion, int reputation) {
        int traitPoints = getTraitPoints(religion);

        int minReputation = 5 - traitPoints;
        int maxReputation = traitPoints - 4;

        if (reputation < minReputation) {
            return minReputation;
        } else if (reputation > maxReputation) {
            return maxReputation;
        }

        return reputation;
    }



    // DEPRECATION DUMP
    @Deprecated public void saveCultures() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(religionFile))) {
            for (Religion culture : religions.values()) {
                String members = String.join(",",
                        culture.getMembers().stream().map(UUID::toString).toArray(String[]::new));
                writer.write(culture.getName() + ":" + culture.getOwner() + ":" + members);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Deprecated private void loadCultures() {
        try (BufferedReader reader = new BufferedReader(new FileReader(religionFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    String name = parts[0];
                    UUID owner = UUID.fromString(parts[1]);
                    List<UUID> members = new ArrayList<>();
                    for (String memberStr : parts[2].split(",")) {
                        members.add(UUID.fromString(memberStr));
                    }
                    //cultures.put(name, new Culture(name, owner, members));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
