package me.sialim.riseoflands;

import me.angeschossen.lands.api.LandsIntegration;
import me.sialim.riseoflands.buildershop.BuilderShop;
import me.sialim.riseoflands.calendar.CalendarPlaceholder;
import me.sialim.riseoflands.calendar.GameCalendar;
import me.sialim.riseoflands.calendar.PlayerDataManager;
import me.sialim.riseoflands.culture.ReligionCommandExecutor;
import me.sialim.riseoflands.culture.ReligionManager;
import me.sialim.riseoflands.culture.trait_events.*;
import me.sialim.riseoflands.discord.DiscordGraveyard;
import me.sialim.riseoflands.government.ReputationManager;
import me.sialim.riseoflands.lands.LandsSpawnManager;
import me.sialim.riseoflands.roleplay.IdentityManager;
import me.sialim.riseoflands.roleplay.IdentityPlaceholder;
import net.advancedplugins.seasons.api.AdvancedSeasonsAPI;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class RiseOfLandsMain extends JavaPlugin {
    public LuckPerms lp;
    public LandsIntegration api;
    public AdvancedSeasonsAPI asAPI;

    public ReputationManager reputationManager;
    public IdentityManager identityManager;
    public GameCalendar calendar;
    public PlayerDataManager playerDataManager;

    public DiscordGraveyard discordGraveyard;
    
    public ReligionCommandExecutor cultureCommandExecutor;
    public ReligionManager religionManager;
    public DietListener dietListener;
    public HolyMobListener holyMobListener;
    public SilenceListener silenceListener;
    public MagicListener magicListener;
    public HolyBlockListener holyBlockListener;
    public RedstoneListener redstoneListener;
    public TameListener tameListener;
    public PacifismListener pacifismListener;
    public LandsSpawnManager lsm;
    public BuilderShop bs;

    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;

    @Override
    public void onEnable() {
        // API registration
        api = LandsIntegration.of(this);

        // Initialization
        playerDataManager = new PlayerDataManager(this);
        reputationManager = new ReputationManager(this);
        identityManager = new IdentityManager(this);
        discordGraveyard = new DiscordGraveyard(this);
        religionManager = new ReligionManager(this, reputationManager);
        cultureCommandExecutor = new ReligionCommandExecutor(religionManager);
        dietListener = new DietListener(this);
        holyMobListener = new HolyMobListener(this);
        silenceListener = new SilenceListener(this);
        magicListener = new MagicListener(this);
        holyBlockListener = new HolyBlockListener(this);
        redstoneListener = new RedstoneListener(this);
        tameListener = new TameListener(this);
        pacifismListener = new PacifismListener(this);
        calendar = new GameCalendar(this, playerDataManager);
        lsm = new LandsSpawnManager(this);
        bs = new BuilderShop(this);

        // Data registration
        saveDefaultConfig();
        identityManager.initializeDataFile();


        // Vault
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();

        // PAPI
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new RoLPlaceholder(this).register();
            new IdentityPlaceholder(this).register();
            new CalendarPlaceholder(this).register();
        }

        // LuckPerms
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            lp = provider.getProvider();
        }

        // Listener registration
        Bukkit.getPluginManager().registerEvents(reputationManager, this);
        Bukkit.getPluginManager().registerEvents(identityManager, this);
        Bukkit.getPluginManager().registerEvents(dietListener, this);
        Bukkit.getPluginManager().registerEvents(holyMobListener, this);
        Bukkit.getPluginManager().registerEvents(silenceListener, this);
        Bukkit.getPluginManager().registerEvents(magicListener, this);
        Bukkit.getPluginManager().registerEvents(holyBlockListener, this);
        Bukkit.getPluginManager().registerEvents(redstoneListener, this);
        Bukkit.getPluginManager().registerEvents(tameListener, this);
        Bukkit.getPluginManager().registerEvents(pacifismListener, this);
        Bukkit.getPluginManager().registerEvents(discordGraveyard, this);
        Bukkit.getPluginManager().registerEvents(calendar, this);
        Bukkit.getPluginManager().registerEvents(lsm, this);
        Bukkit.getPluginManager().registerEvents(bs, this);
        //Bukkit.getPluginManager().registerEvents(, this);

        // Command registration
        //getCommand("religion").setExecutor(cultureCommandExecutor);
        //getCommand("religion").setTabCompleter(cultureCommandExecutor);
        getCommand("identity").setExecutor(identityManager);
        getCommand("setdate").setExecutor(calendar);
        getCommand("pause").setExecutor(calendar);
        getCommand("resume").setExecutor(calendar);
        getCommand("date").setExecutor(calendar);
        getCommand("date").setTabCompleter(calendar);
        getCommand("buildershop").setExecutor(bs);

        // Timer registration
        minuteTimer();

        RegisteredServiceProvider<LuckPerms> otherProvider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (otherProvider != null) {
            lp = otherProvider.getProvider();

        }
    }

    @Override
    public void onDisable() {
        // Data storage
        reputationManager.saveLandReputation();
        reputationManager.savePlayerReputation();
        identityManager.savePlayerData();
        religionManager.saveCulturesToJson();
        religionManager.saveCooldownsToFile();
        religionManager.saveLandReligions();
        playerDataManager.savePlayerData();
    }

    public void minuteTimer() {
        new BukkitRunnable() {
            @Override public void run() {
                resetPlayerReputations();
                setPlayerSizes();
            }
        }.runTaskTimer(this, 0L, 1200L);
    }

    public void resetPlayerReputations() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            //religionManager.resetReputation(player.getUniqueId());
            religionManager.forgive(player.getUniqueId());
        }
    }

    public void setPlayerSizes() {
        for (Player player :Bukkit.getOnlinePlayers()) {
            identityManager.setSize(player.getUniqueId(), identityManager.calculateSize(player.getUniqueId()));
        }
    }

    public Economy getEconomy() {
        return econ;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
}
