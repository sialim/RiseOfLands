package me.sialim.riseoflands;

import me.angeschossen.lands.api.LandsIntegration;
import me.sialim.riseoflands.culture.ReligionCommandExecutor;
import me.sialim.riseoflands.culture.ReligionManager;
import me.sialim.riseoflands.culture.trait_events.*;
import me.sialim.riseoflands.government.ReputationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class RiseOfLands extends JavaPlugin {
    public LandsIntegration api;
    
    public ReputationManager reputationManager;
    
    public ReligionCommandExecutor cultureCommandExecutor;
    public ReligionManager religionManager;
    public DietListener dietListener;
    public HolyMobListener holyMobListener;
    public SilenceListener silenceListener;
    public MagicListener magicListener;
    public HolyBlockListener holyBlockListener;

    @Override
    public void onEnable() {
        // Initialization
        reputationManager = new ReputationManager(this);
        religionManager = new ReligionManager(this, reputationManager);
        cultureCommandExecutor = new ReligionCommandExecutor(religionManager);
        dietListener = new DietListener(this);
        holyMobListener = new HolyMobListener(this);
        silenceListener = new SilenceListener(this);
        magicListener = new MagicListener(this);
        holyBlockListener = new HolyBlockListener(this);

        // PAPI
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            new RoLPlaceholder(this).register();

        // Listener registration
        Bukkit.getPluginManager().registerEvents(reputationManager, this);
        Bukkit.getPluginManager().registerEvents(dietListener, this);
        Bukkit.getPluginManager().registerEvents(holyMobListener, this);
        Bukkit.getPluginManager().registerEvents(silenceListener, this);
        Bukkit.getPluginManager().registerEvents(magicListener, this);
        Bukkit.getPluginManager().registerEvents(holyBlockListener, this);

        // Command registration
        getCommand("religion").setExecutor(cultureCommandExecutor);
        getCommand("religion").setTabCompleter(cultureCommandExecutor);

        // Timer registration
        minuteTimer();

        api = LandsIntegration.of(this);
    }

    @Override
    public void onDisable() {
        // Data storage
        reputationManager.saveLandReputation();
        reputationManager.savePlayerReputation();
        religionManager.saveCulturesToJson();
        religionManager.saveCooldownsToFile();
    }

    public void minuteTimer() {
        new BukkitRunnable() {
            @Override public void run() {
                resetPlayerReputations();
            }
        }.runTaskTimer(this, 0L, 1200L);
    }

    public void resetPlayerReputations() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            religionManager.resetReputation(player.getUniqueId());
            religionManager.forgive(player.getUniqueId());
        }
    }
}
