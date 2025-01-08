package me.sialim.riseoflands.culture;

import java.io.*;
import java.util.*;

public class CultureManager {
    private final String filePath;
    private final Map<String, Culture> cultures;

    public CultureManager(String filePath) {
        this.filePath = filePath;
        this.cultures = new HashMap<>();
        loadCultures();
    }

    private void loadCultures() {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    String name = parts[0];
                    String owner = parts[1];
                    List<String> members = new ArrayList<>(Arrays.asList(parts[2].split(",")));
                    cultures.put(name, new Culture(name, owner, members));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCultures() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Culture culture : cultures.values()) {
                String members = String.join(",", culture.getMembers());
                writer.write(culture.getName() + ":" + culture.getOwner() + ":" + members);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String createCulture(String playerName, String cultureName) {
        if (cultures.containsKey(cultureName)) {
            return "Culture already exists.";
        }
        cultures.put(cultureName, new Culture(cultureName, playerName, Collections.singletonList(playerName)));
        saveCultures();
        return "Culture " + cultureName + " created by " + playerName + ".";
    }

    public String joinCulture(String playerName, String cultureName) {
        Culture culture = cultures.get(cultureName);
        if (culture == null)
            return "Culture does not exist.";

        if (isPlayerInAnyCulture(playerName))
            return "You are already in a culture.";

        culture.addMember(playerName);
        saveCultures();
        return playerName + " joined the culture " + cultureName + ".";
    }

    public String leaveCulture(String playerName) {
        for (Culture culture : cultures.values()) {
            if (culture.getMembers().contains(playerName)) {
                culture.removeMember(playerName);
                saveCultures();
                return playerName + " left the culture " + culture.getName() + ".";
            }
        }
        return "Player is not in any culture.";
    }

    private boolean isPlayerInAnyCulture(String playerName) {
        for (Culture culture : cultures.values()) {
            if (culture.getMembers().contains(playerName)) {
                return true;
            }
        }
        return false;
    }

    public String getPlayerCulture(String playerName) {
        for (Culture culture : getCultureNames().stream().map(this::getCulture).toList()) {
            if (culture.getMembers().contains(playerName))
                return culture.getName();
        }
        return null;
    }

    public Culture getCulture(String cultureName) {
        return cultures.get(cultureName);
    }

    public String deleteCulture(String playerName, String cultureName) {
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
