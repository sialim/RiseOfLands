package me.sialim.riseoflands;

import me.angeschossen.lands.api.LandsIntegration;
import me.sialim.riseoflands.culture.CultureCommandExecutor;
import me.sialim.riseoflands.culture.CultureManager;
import me.sialim.riseoflands.government.ReputationManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class RiseOfLands extends JavaPlugin {
    public LandsIntegration api;
    
    public ReputationManager reputationManager;
    
    public CultureCommandExecutor cultureCommandExecutor;
    public CultureManager cultureManager;

    @Override
    public void onEnable() {
        // Initialization
        reputationManager = new ReputationManager(this);
        cultureManager = new CultureManager(this);
        cultureCommandExecutor = new CultureCommandExecutor(cultureManager);

        // Listener registration
        Bukkit.getPluginManager().registerEvents(reputationManager, this);

        // Command registration
        getCommand("culture").setExecutor(cultureCommandExecutor);
        getCommand("culture").setTabCompleter(cultureCommandExecutor);

        api = LandsIntegration.of(this);
    }

    @Override
    public void onDisable() {
        // Data storage
        reputationManager.saveLandReputation();
        reputationManager.savePlayerReputation();
        cultureManager.saveCultures();
    }
}
