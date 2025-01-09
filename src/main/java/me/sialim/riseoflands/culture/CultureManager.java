package me.sialim.riseoflands.culture;

import me.sialim.riseoflands.RiseOfLands;

import java.io.*;
import java.util.*;

public class CultureManager {
    private final File cultureFile;
    private final Map<String, Culture> cultures;

    public CultureManager(RiseOfLands plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.cultureFile = new File(plugin.getDataFolder(), "cultures.txt");
        this.cultures = new HashMap<>();
        if (cultureFile.exists()) {
            loadCultures();
        } else {
            try {
                cultureFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create cultures.txt file: " + e.getMessage());
            }
        }
    }

    private void loadCultures() {
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
                    cultures.put(name, new Culture(name, owner, members));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCultures() {
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

    public String createCulture(UUID playerName, String cultureName) {
        for (Culture culture : cultures.values()) {
            if (culture.getMembers().contains(playerName))
                return "You are already part of a culture and cannot create a new one.";
        }

        if (cultures.containsKey(cultureName))
            return "Culture already exists.";

        cultures.put(cultureName, new Culture(cultureName, playerName, Collections.singletonList(playerName)));
        saveCultures();
        return "Culture " + cultureName + " created by " + playerName + ".";
    }

    public String joinCulture(UUID playerName, String cultureName) {
        Culture culture = cultures.get(cultureName);
        if (culture == null)
            return "Culture does not exist.";

        if (isPlayerInAnyCulture(playerName))
            return "You are already in a culture.";

        culture.addMember(playerName);
        saveCultures();
        return playerName + " joined the culture " + cultureName + ".";
    }

    public String leaveCulture(UUID playerName) {
        for (Culture culture : cultures.values()) {
            if (culture.getMembers().contains(playerName)) {
                if (culture.getOwner().equals(playerName))
                    return "You cannot leave your culture because you are the owner. Consider deleting it instead.";
                culture.removeMember(playerName);
                saveCultures();
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

        cultures.remove(cultureName);
        saveCultures();
        return "Culture " + cultureName + " has been deleted.";
    }

    public List<String> getCultureNames() {
        return new ArrayList<>(cultures.keySet());
    }

}
