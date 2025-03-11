package me.sialim.riseoflands.government;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.events.LandCreateEvent;
import me.angeschossen.lands.api.events.LandTrustPlayerEvent;
import me.angeschossen.lands.api.events.LandUntrustPlayerEvent;
import me.angeschossen.lands.api.land.Land;
import me.sialim.riseoflands.RiseOfLandsMain;
import me.sialim.riseoflands.culture.ReligionManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ReputationManager implements Listener {
    private final RiseOfLandsMain plugin;
    private final File playerRepFile;
    private final File landsRepFile;
    private final Map<UUID, Integer> playerReputation = new ConcurrentHashMap<>();
    private final Map<Land, Double> landReputation = new ConcurrentHashMap<>();

    public ReputationManager(RiseOfLandsMain plugin) {
        this.plugin = plugin;
        this.playerRepFile = new File(plugin.getDataFolder(), "playerReputation.txt");
        this.landsRepFile = new File(plugin.getDataFolder(), "landReputation.txt");
        loadPlayerReputation();
        loadLandReputation();
    }

    private void loadPlayerReputation() {
        try {
            if (!playerRepFile.exists()) {
                playerRepFile.getParentFile().mkdirs();
                playerRepFile.createNewFile();
            }
            List<String> lines = Files.readAllLines(Paths.get(playerRepFile.getPath()));
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    UUID uuid = UUID.fromString(parts[0]);
                    int reputation = Integer.parseInt(parts[1]);
                    playerReputation.put(uuid, reputation);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePlayerReputation() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(playerRepFile))) {
            for (Map.Entry<UUID, Integer> entry : playerReputation.entrySet()) {
                UUID uuid = entry.getKey();
                int reputation = entry.getValue();
                writer.write(uuid + "," + reputation);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLandReputation() {
        try {
            if (!landsRepFile.exists()) {
                landsRepFile.getParentFile().mkdirs();
                landsRepFile.createNewFile();
            }
            List<String> lines = Files.readAllLines(Paths.get(landsRepFile.getPath()));
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    Land land = plugin.api.getLandByName(parts[0]);
                    double reputation = Double.parseDouble(parts[1]);
                    landReputation.put(land, reputation);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveLandReputation() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(landsRepFile))) {
            for (Map.Entry<Land, Double> entry : landReputation.entrySet()) {
                String landName = entry.getKey().getName();
                double reputation = entry.getValue();
                writer.write(landName + "," + reputation);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPlayerReputation(UUID uuid) {
        return playerReputation.get(uuid);
    }

    public void setPlayerReputation(UUID uuid, int reputation) {
        playerReputation.put(uuid, reputation);
        savePlayerReputation();
    }

    public double getLandReputation(Land land) {
        int baseReputation = land.getTrustedPlayers().stream()
                .mapToInt(this::getPlayerReputation)
                .sum();
        double modifier = getLandReputationModifier(land);
        return baseReputation * modifier;
    }

    public double getLandReputationModifier(Land land) {
        return landReputation.getOrDefault(land, 1.0);
    }

    public void setLandReputationModifier(Land land, double modifier) {
        landReputation.put(land, modifier);
        saveLandReputation();
    }

    public double getLandTrueReputation(Land land) {
        return land.getTrustedPlayers().stream()
                .mapToInt(this::getPlayerReputation)
                .sum();
    }

    public void resetPlayerReputation(UUID uuid) {
        playerReputation.remove(uuid);
        savePlayerReputation();
    }

    public void resetLandReputation(Land land) {
        landReputation.remove(land);
        saveLandReputation();
    }

    public void setMaxChunks(Player p, int maxChunks) {
        LuckPerms api = plugin.lp;
        User user = api.getUserManager().getUser(p.getUniqueId());

        if (user == null) {
            plugin.getLogger().warning("LuckPerms user not found for " + p.getName());
            return;
        }

        user.getNodes().stream()
                .filter(node -> node.getKey().startsWith("lands.chunks.max"))
                .forEach(node -> user.data().remove(node));

        Node newPermission = Node.builder("lands.chunk.max" + maxChunks).build();
        user.data().add(newPermission);
    }

    @EventHandler public void onLandCreate(LandCreateEvent e) {
        Land land = e.getLand();
        landReputation.put(land, 1.0);
        saveLandReputation();
    }

    @EventHandler public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!playerReputation.containsKey(player.getUniqueId())) {
            playerReputation.put(player.getUniqueId(), 5);
            savePlayerReputation();
        }
    }
}
