package me.sialim.riseoflands.war;

import me.angeschossen.lands.api.land.Land;
import me.sialim.riseoflands.RiseOfLandsMain;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FortManager {
    RiseOfLandsMain plugin;

    private Map<String, List<Chunk>> fortChunks = new HashMap<>();

    private int reputationCostPerChunk;
    public FortManager(RiseOfLandsMain plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        reputationCostPerChunk = config.getInt("fort.reputation_cost_per_chunk", 50);
    }

    public void initializeLand(Land land, Chunk spawnChunk) {
        if (!fortChunks.containsKey(land.getName())) {
            List<Chunk> chunks = new ArrayList<>();
            chunks.add(spawnChunk);
            fortChunks.put(land.getName(), chunks);
        }
    }
}
