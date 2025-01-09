package me.sialim.riseoflands.culture;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.sialim.riseoflands.RiseOfLands;
import me.sialim.riseoflands.culture.traits.CannibalCTrait;
import me.sialim.riseoflands.culture.traits.CarnivoreCTrait;
import me.sialim.riseoflands.culture.traits.PassiveAnimalsCTrait;
import me.sialim.riseoflands.government.ReputationManager;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CultureManager {
    private final File cultureFile;
    private final Map<String, Culture> cultures;
    private Map<UUID, CultureCooldown> cooldowns;
    private ReputationManager reputationManager;

    public CultureManager(RiseOfLands plugin, ReputationManager reputationManager) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.reputationManager = reputationManager;
        this.cultureFile = new File(plugin.getDataFolder(), "cultures.txt");
        this.cultures = new HashMap<>();
        if (cultureFile.exists()) {
            loadCulturesFromJson();
        } else {
            try {
                cultureFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create cultures.txt file: " + e.getMessage());
            }
        }
    }

    @Deprecated private void loadCultures() {
        try (BufferedReader reader = new BufferedReader(new FileReader(cultureFile))) {
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

    private void loadCulturesFromJson() {
        try (BufferedReader reader = new BufferedReader(new FileReader(cultureFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 4) {
                    String name = parts[0];
                    UUID owner = UUID.fromString(parts[1]);
                    List<UUID> members = new ArrayList<>();
                    for (String memberStr : parts[2].split(",")) {
                        members.add(UUID.fromString(memberStr));
                    }

                    // Deserialize traits from JSON
                    List<CTrait> traits = new ArrayList<>();
                    List<Map<String, Object>> traitsMaps = new Gson().fromJson(parts[3], new TypeToken<List<Map<String, Object>>>(){}.getType());
                    for (Map<String, Object> traitMap : traitsMaps) {
                        traits.add(CTrait.fromMap(traitMap));
                    }

                    cultures.put(name, new Culture(name, owner, members, traits));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Deprecated public void saveCultures() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cultureFile))) {
            for (Culture culture : cultures.values()) {
                String members = String.join(",",
                        culture.getMembers().stream().map(UUID::toString).toArray(String[]::new));
                writer.write(culture.getName() + ":" + culture.getOwner() + ":" + members);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCulturesToJson() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cultureFile))) {
            for (Culture culture : cultures.values()) {
                String members = String.join(",", culture.getMembers().stream().map(UUID::toString).toArray(String[]::new));

                // Convert traits to map representation
                List<Map<String, Object>> traitsMaps = new ArrayList<>();
                for (CTrait trait : culture.getTraits()) {
                    traitsMaps.add(trait.toMap());
                }

                // Convert the list of maps into a JSON string
                String traitsJson = new Gson().toJson(traitsMaps);

                writer.write(culture.getName() + ":" + culture.getOwner() + ":" + members + ":" + traitsJson);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String createCulture(UUID playerName, String cultureName) {
        for (Culture culture : cultures.values()) {
            if (culture.getMembers().contains(playerName))
                return "You are already part of a culture and cannot create a new one.";
        }

        if (cultures.containsKey(cultureName))
            return "Culture already exists.";


        CultureCooldown cooldown = cooldowns.getOrDefault(playerName, new CultureCooldown());
        long currentTime = System.currentTimeMillis();

        if (currentTime - cooldown.getCultureDeleteTime() < TimeUnit.DAYS.toMillis(2))
            return "You cannot create a culture for another " +
                    ((TimeUnit.DAYS.toMillis(2) - (currentTime - cooldown.getCultureDeleteTime())) / 1000 / 60 / 60) +
                    " hours.";

        cooldown.setReputationReset(false);
        cooldown.setCultureCreateTime(System.currentTimeMillis());
        cooldown.setCultureJoinTime(System.currentTimeMillis());
        cooldowns.put(playerName, cooldown);

        // Remove 5 rep points
        int currentRep = reputationManager.getPlayerReputation(playerName);
        reputationManager.setPlayerReputation(playerName, currentRep - 5);

        List<CTrait> sampleTraits = Arrays.asList(new CarnivoreCTrait(), new PassiveAnimalsCTrait());

        cultures.put(cultureName, new Culture(cultureName, playerName, Collections.singletonList(playerName), sampleTraits));
        saveCulturesToJson();
        return "Culture " + cultureName + " created by " + playerName + ".";
    }

    public String joinCulture(UUID playerName, String cultureName) {
        Culture culture = cultures.get(cultureName);
        if (culture == null)
            return "Culture does not exist.";

        if (isPlayerInAnyCulture(playerName))
            return "You are already in a culture.";

        CultureCooldown cooldown = cooldowns.getOrDefault(playerName, new CultureCooldown());

        if (System.currentTimeMillis() - cooldown.getCultureLeaveTime() < TimeUnit.DAYS.toMillis(2)) {
            return "You cannot join another culture for another " +
                    ((TimeUnit.DAYS.toMillis(2) - (System.currentTimeMillis() - cooldown.getCultureLeaveTime())) / 1000 / 60 / 60) +
                    " hours.";
        }

        cooldown.setReputationReset(false);
        cooldown.setCultureJoinTime(System.currentTimeMillis());
        cooldowns.put(playerName, cooldown);

        culture.addMember(playerName);
        saveCulturesToJson();
        return playerName + " joined the culture " + cultureName + ".";
    }

    public String leaveCulture(UUID playerName) {
        for (Culture culture : cultures.values()) {
            if (culture.getMembers().contains(playerName)) {
                if (culture.getOwner().equals(playerName))
                    return "You cannot leave your culture because you are the owner. Consider deleting it instead.";

                CultureCooldown cooldown = cooldowns.getOrDefault(playerName, new CultureCooldown());

                if (System.currentTimeMillis() - cooldown.getCultureJoinTime() < TimeUnit.DAYS.toMillis(1)) {
                    return "You cannot leave the culture until " +
                            ((TimeUnit.DAYS.toMillis(1) - (System.currentTimeMillis() - cooldown.getCultureJoinTime())) / 1000 / 60 / 60) +
                            " hours.";
                }

                cooldown.setCultureLeaveTime(System.currentTimeMillis());
                cooldowns.put(playerName, cooldown);

                culture.removeMember(playerName);
                saveCulturesToJson();
                return playerName + " left the culture " + culture.getName() + ".";
            }
        }
        return "Player is not in any culture.";
    }

    private boolean isPlayerInAnyCulture(UUID playerName) {
        for (Culture culture : cultures.values()) {
            if (culture.getMembers().contains(playerName)) {
                return true;
            }
        }
        return false;
    }

    public String getPlayerCulture(UUID playerName) {
        for (Culture culture : getCultureNames().stream().map(this::getCulture).toList()) {
            if (culture.getMembers().contains(playerName))
                return culture.getName();
        }
        return null;
    }

    public Culture getCulture(String cultureName) {
        return cultures.get(cultureName);
    }

    public String deleteCulture(UUID playerName, String cultureName) {
        Culture culture = cultures.get(cultureName);
        if (culture == null)
            return "Culture does not exist.";

        if (!culture.getOwner().equals(playerName))
            return "You are not the owner of this culture.";

        CultureCooldown cooldown = cooldowns.getOrDefault(playerName, new CultureCooldown());
        cooldown.setCultureDeleteTime(System.currentTimeMillis());
        cooldown.setCultureLeaveTime(System.currentTimeMillis());
        cooldowns.put(playerName, cooldown);

        // Remove 5 rep points
        int currentRep = reputationManager.getPlayerReputation(playerName);
        reputationManager.setPlayerReputation(playerName, currentRep - 5);

        cultures.remove(cultureName);
        saveCulturesToJson();
        return "Culture " + cultureName + " has been deleted.";
    }

    public void resetReputation(UUID playerName) {
        CultureCooldown cooldown = cooldowns.getOrDefault(playerName, new CultureCooldown());
        long currentTime = System.currentTimeMillis();

        if (!cooldown.getReputationReset() && currentTime - cooldown.getCultureDeleteTime() >= TimeUnit.DAYS.toMillis(2)) {
            reputationManager.setPlayerReputation(playerName, 5);
            cooldown.setReputationReset(true);
            cooldowns.put(playerName, cooldown);
        }
    }

    public List<String> getCultureNames() {
        return new ArrayList<>(cultures.keySet());
    }
}
