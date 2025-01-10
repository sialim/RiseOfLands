package me.sialim.riseoflands.culture;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.sialim.riseoflands.RiseOfLands;
import me.sialim.riseoflands.culture.traits.CarnivoreCTrait;
import me.sialim.riseoflands.culture.traits.PassiveAnimalsCTrait;
import me.sialim.riseoflands.government.ReputationManager;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ReligionManager {
    private final File religionFile;
    private final File cooldownFile;
    private final Map<String, Religion> religions;
    public Map<UUID, ReligionCooldown> cooldowns;
    private ReputationManager reputationManager;

    public ReligionManager(RiseOfLands plugin, ReputationManager reputationManager) {
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

    private void loadCulturesFromJson() {
        try (BufferedReader reader = new BufferedReader(new FileReader(religionFile))) {
            String line;
            Gson gson = new Gson();

            while ((line = reader.readLine()) != null) {
                Map<String, Object> cultureData = gson.fromJson(line, Map.class);
                String cultureName = (String) cultureData.get("name");
                UUID owner = UUID.fromString((String) cultureData.get("owner"));
                List<UUID> members = ((List<String>) cultureData.get("members")).stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList());

                // Deserialize the traits list
                List<Map<String, Object>> traitsMap = (List<Map<String, Object>>) cultureData.get("traits");
                List<RTrait> traits = new ArrayList<>();
                for (Map<String, Object> traitMap : traitsMap) {
                    traits.add(RTrait.fromMap(traitMap));  // Reconstruct each trait from its map
                }

                // Create the Religion object
                religions.put(cultureName, new Religion(cultureName, owner, members, traits));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadCooldownsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(cooldownFile))) {
            Gson gson = new Gson();
            String json = reader.readLine();

            if (json != null && !json.isEmpty()) {
                this.cooldowns = gson.fromJson(json, new TypeToken<Map<UUID, ReligionCooldown>>(){}.getType());
            }
            // Ensure cooldowns is initialized if JSON is null/empty
            if (this.cooldowns == null) {
                this.cooldowns = new HashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.cooldowns = new HashMap<>(); // Initialize in case of failure
        }
    }

    public void saveCulturesToJson() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(religionFile))) {
            Gson gson = new Gson();
            for (Religion culture : religions.values()) {
                // Convert the traits in the religion to maps
                List<Map<String, Object>> traitsMap = new ArrayList<>();
                for (RTrait trait : culture.getTraits()) {
                    traitsMap.add(trait.toMap());
                }

                // Create a Map for the culture data and serialize
                Map<String, Object> cultureData = new HashMap<>();
                cultureData.put("name", culture.getName());
                cultureData.put("owner", culture.getOwner().toString());
                cultureData.put("members", culture.getMembers().stream().map(UUID::toString).collect(Collectors.toList()));
                cultureData.put("traits", traitsMap);  // Add traits data

                // Serialize the culture
                String json = gson.toJson(cultureData);
                writer.write(json);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCooldownsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cooldownFile))) {
            Gson gson = new Gson();

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

        List<RTrait> sampleTraits = Arrays.asList(new CarnivoreCTrait(), new PassiveAnimalsCTrait());

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

    public String getPlayerCulture(UUID playerName) {
        for (Religion culture : getCultureNames().stream().map(this::getCulture).toList()) {
            if (culture.getMembers().contains(playerName))
                return culture.getName();
        }
        return null;
    }

    public Religion getCulture(String cultureName) {
        return religions.get(cultureName);
    }

    public String deleteCulture(UUID playerName, String cultureName) {
        Religion culture = religions.get(cultureName);
        if (culture == null)
            return "Religion does not exist.";

        if (!culture.getOwner().equals(playerName))
            return "You are not the owner of this religion.";

        ReligionCooldown cooldown = cooldowns.getOrDefault(playerName, new ReligionCooldown());
        cooldown.setCultureDeleteTime(System.currentTimeMillis());
        cooldown.setCultureLeaveTime(System.currentTimeMillis());
        cooldowns.put(playerName, cooldown);
        saveCooldownsToFile();

        for (UUID player : culture.getMembers()) {
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
            reputationManager.setPlayerReputation(playerName, 5);
            cooldown.setReputationReset(true);
            cooldowns.put(playerName, cooldown);
            saveCooldownsToFile();
            String username = Bukkit.getPlayer(playerName).getName();
            Bukkit.getLogger().info(username + " has had their reputation reset after deleting their religion.");
        }
    }

    public void forgive(UUID playerName) {
        ReligionCooldown cooldown = cooldowns.getOrDefault(playerName, new ReligionCooldown());
        long currentTime = System.currentTimeMillis();

        if ((currentTime - cooldown.getForgivenessTimer() >= TimeUnit.DAYS.toMillis(2))
        && currentTime - cooldown.getCultureJoinTime() >= TimeUnit.DAYS.toMillis(1)) {
            reputationManager.setPlayerReputation(playerName, 5);
            cooldown.setReputationReset(true);
            cooldowns.put(playerName, cooldown);
            saveCooldownsToFile();
            String username = Bukkit.getPlayer(playerName).getName();
            Bukkit.getLogger().info(username + " has had their reputation reset after deleting their religion.");
        }
    }


    public List<String> getCultureNames() {
        return new ArrayList<>(religions.keySet());
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
