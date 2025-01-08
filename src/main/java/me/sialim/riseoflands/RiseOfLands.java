package me.sialim.riseoflands;

import me.angeschossen.lands.api.LandsIntegration;
import me.sialim.riseoflands.government.ReputationManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class RiseOfLands extends JavaPlugin {
    public LandsIntegration api;
    public ReputationManager reputationManager;

    @Override
    public void onEnable() {
        // Initialization
        reputationManager = new ReputationManager(this);

        api = LandsIntegration.of(this);
    }

    @Override
    public void onDisable() {
        // Data storage
        reputationManager.saveLandReputation();
        reputationManager.savePlayerReputation();
    }
}
