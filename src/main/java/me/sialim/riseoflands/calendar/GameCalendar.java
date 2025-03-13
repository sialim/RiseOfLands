package me.sialim.riseoflands.calendar;

import me.sialim.riseoflands.RiseOfLandsMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GameCalendar implements Listener, CommandExecutor, TabCompleter {
    public Map<String, LocalDate> worldDates = new HashMap<>();
    public Map<String, Long> lastDayTicks = new HashMap<>();
    RiseOfLandsMain plugin;

    public PlayerDataManager playerDataManager;

    private static final String DATA_FOLDER = "calendar_data/";
    private static final String DATE_FOLDER = DATA_FOLDER + "world_dates/";
    private static final String TICK_FILE = DATA_FOLDER + "world_ticks/";

    private boolean isPaused = false;

    public GameCalendar(RiseOfLandsMain plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        loadWorldData();
        startCalendarUpdater();
        startSeasonUpdater();
    }

    private void startCalendarUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    for (World world : Bukkit.getWorlds()) {
                        updateWorldDate(world);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void startSeasonUpdater() {
        if (!isPaused) {
            updateSeason(Bukkit.getWorld(plugin.getConfig().getString("main-world")));
        }
    }

    private void loadWorldData() {
        File dateFolder = new File(plugin.getDataFolder(), DATE_FOLDER);
        if (!dateFolder.exists()) dateFolder.mkdirs();

        File tickFolder = new File(plugin.getDataFolder(), TICK_FILE);
        if (!tickFolder.exists()) tickFolder.mkdirs();

        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();
            worldDates.put(worldName, loadDate(worldName));
            lastDayTicks.put(worldName, loadLastDayTick(worldName, world.getFullTime()));
        }
    }

    private void saveWorldData() {
        File dateFolder = new File(plugin.getDataFolder(), DATE_FOLDER);
        if (!dateFolder.exists()) dateFolder.mkdirs();

        File tickFolder = new File(plugin.getDataFolder(), TICK_FILE);
        if (!tickFolder.exists()) tickFolder.mkdirs();

        for (String world : worldDates.keySet()) {
            saveDate(world, worldDates.get(world));
            saveLastDayTick(world, lastDayTicks.get(world));
        }
    }

    private LocalDate loadDate(String world) {
        File file = new File(plugin.getDataFolder(), DATE_FOLDER + world + ".txt");
        if (!file.exists()) return LocalDate.of(476, 1, 1);

        try {
            return LocalDate.parse(Files.readString(file.toPath()).trim());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load date for world: " + world);
            return LocalDate.of(476, 1, 1);
        }
    }

    private void saveDate(String world, LocalDate date) {
        File file = new File(plugin.getDataFolder(), DATE_FOLDER + world + ".txt");
        try {
            Files.writeString(file.toPath(), date.toString());
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save date for world: " + world);
        }
    }

    private long loadLastDayTick(String world, long defaultTick) {
        File file = new File(plugin.getDataFolder(), TICK_FILE + world + ".txt");
        if (!file.exists()) return defaultTick;

        try {
            return Long.parseLong(Files.readString(file.toPath()).trim());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load last tick for world: " + world);
            return defaultTick;
        }
    }

    private void saveLastDayTick(String world, long tick) {
        File file = new File(plugin.getDataFolder(), TICK_FILE + world + ".txt");
        try {
            Files.writeString(file.toPath(), String.valueOf(tick));
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save last tick for world: " + world);
        }
    }

    private void updateWorldDate(World world) {
        long currentTime = world.getTime();
        String worldName = world.getName();

        if (currentTime >= 18000 && lastDayTicks.getOrDefault(worldName, -1L) < 18000) {
            LocalDate newDate = worldDates.getOrDefault(worldName, LocalDate.of(476, 1, 1)).plusDays(1);
            worldDates.put(worldName, newDate);
            lastDayTicks.put(worldName, currentTime);
            plugin.getLogger().info("New day in " + worldName + ": " + newDate);
        } else {
            lastDayTicks.put(worldName, currentTime);
        }
    }

    private void updateSeason(World world) {
        LocalDate date = worldDates.getOrDefault(world.getName(), LocalDate.of(476, 1, 1));
        updateSeasons(world, date);
    }

    private void updateSeasons(World world, LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        String command = "advancedseasons setseason ";

        if ((month == 12 && day < 23)) {
            command += "FALL_TRANSITION_3";
        } else if ((month == 12 && day >= 23) || (month == 1 && day < 14)) {
            command += "WINTER";
        } else if ((month == 1 && day >= 14) || (month == 2 && day < 5)) {
            command += "WINTER_TRANSITION_1";
        } else if (month == 2 && day >= 5) {
            command += "WINTER_TRANSITION_2";
        }

        else if ((month == 3 && day < 24)) {
            command += "WINTER_TRANSITION_3";
        } else if ((month == 3 && day >= 24) || (month == 4 && day < 16)) {
            command += "SPRING";
        } else if ((month == 4 && day >= 16) && (month == 5 && day < 9)) {
            command += "SPRING_TRANSITION_1";
        } else if (month == 5 && day >= 9) {
            command += "SPRING_TRANSITION_2";
        }

        else if ((month == 6 && day < 24)) {
            command += "SPRING_TRANSITION_3";
        } else if ((month == 6 && day >= 24) && (month == 7 && day < 17)) {
            command += "SUMMER";
        } else if ((month == 7 && day >= 17) && (month == 8 && day < 9)) {
            command += "SUMMER_TRANSITION_1";
        } else if (month == 8 && day >= 9) {
            command += "SUMMER_TRANSITION_2";
        }

        else if ((month == 9 && day < 24)) {
            command += "SUMMER_TRANSITION_3";
        } else if ((month == 9 && day >= 24) && (month == 10 && day < 17)) {
            command += "FALL";
        } else if ((month == 10 && day >= 17) && (month == 11 && day < 9)) {
            command += "FALL_TRANSITION_1";
        } else if (month == 11 && day >= 9) {
            command += "FALL_TRANSITION_2";
        } else {
            return;
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command + " " + plugin.getConfig().getString("main-world"));
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        saveWorldData();
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        String world = event.getWorld().getName();
        worldDates.putIfAbsent(world, LocalDate.of(476, 1, 1));
        lastDayTicks.putIfAbsent(world, event.getWorld().getFullTime());
    }

    @EventHandler
    public void onTimeSkip(TimeSkipEvent e) {
        if (e.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            World w = e.getWorld();
            long currentTime = w.getTime();

            if (currentTime < 18000) {
                LocalDate date = worldDates.getOrDefault(w.getName(), LocalDate.of(476, 1, 1));
                date = date.plusDays(1);
                worldDates.put(w.getName(), date);
            }
        }
    }

    private String getDayWithSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return day + "th";
        }
        switch (day % 10) {
            case 1: return day + "st";
            case 2: return day + "nd";
            case 3: return day + "rd";
            default: return day + "th";
        }
    }

    public String getFormattedDate(World world) {
        LocalDate date = worldDates.getOrDefault(world.getName(), LocalDate.of(476, 1, 1));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM");
        String month = date.format(formatter);
        String dayWithSuffix = getDayWithSuffix(date.getDayOfMonth());
        int year = date.getYear();
        return month + " " + dayWithSuffix + ", " + year;
    }

    public String getFormattedDate(LocalDate date) {
        if (date == null) return "Unknown date";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM");
        String month = date.format(formatter);
        String dayWithSuffix = getDayWithSuffix(date.getDayOfMonth());
        int year = date.getYear();
        return month + " " + dayWithSuffix + ", " + year;
    }

    public String getFormattedTime(World world, Player p) {
        long currentTick = world.getTime();
        long ticksInDay = currentTick % 24000;

        long adjustedTicks = (ticksInDay + 6000) % 24000;

        long totalMinutes = (adjustedTicks * 1440) / 24000;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        PlayerDataManager.PlayerPreferences preferences = playerDataManager.getPlayerPreferences(p.getUniqueId());
        String timeFormat = preferences.getTimeFormat().equals("24") ? "HH:mm" : "h:mm a";

        LocalTime time = LocalTime.of((int) hours, (int) minutes);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeFormat);

        return time.format(formatter);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {
            UUID uuid = p.getUniqueId();
            PlayerDataManager.PlayerPreferences preferences = playerDataManager.getPlayerPreferences(uuid);

            if (command.getName().equals("date") && args.length == 2 && args[0].equals("format")) {
                preferences.setDateFormat(args[1]);
                playerDataManager.setPlayerPreferences(uuid, preferences);
                p.sendMessage(ChatColor.GREEN + "Date format set to " + args[1]);
                return true;
            }
            if (command.getName().equals("temperature") && args.length == 2 && args[0].equals("set")) {
                String unit = args[1].toLowerCase();
                if (unit.equals("celsius") || unit.equals("fahrenheit")) {
                    preferences.setTemperatureUnit(unit);
                    playerDataManager.setPlayerPreferences(uuid, preferences);
                    p.sendMessage(ChatColor.GREEN + "Temperature unit set to " + unit);
                    return true;
                } else {
                    p.sendMessage(ChatColor.RED + "Invalid temperature unit. Use 'celsius' or 'fahrenheit'.");
                    return false;
                }
            }
            if (command.getName().equals("date") && args.length == 2 && args[0].equals("time")) {
                if (args[1].equals("12") || args[1].equals("24")) {
                    preferences.setTimeFormat(args[1]);
                    playerDataManager.setPlayerPreferences(uuid, preferences);
                    p.sendMessage(ChatColor.GREEN + "Time format set to " + args[1] + "-hour");
                    return true;
                } else {
                    p.sendMessage(ChatColor.RED + "Invalid time format. Use '12' or '24'.");
                    return false;
                }
            }
            if (command.getName().equals("date") && args.length == 1 && args[0].equals("reload")) {
                plugin.reloadConfig();
            }
        }
        if (command.getName().equalsIgnoreCase("pause") && sender.hasPermission("calendar.pause")) {
            isPaused = true;
            sender.sendMessage(ChatColor.GREEN + "Calendar paused.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("resume") && sender.hasPermission("calendar.resume")) {
            isPaused = false;
            sender.sendMessage(ChatColor.GREEN + "Calendar resumed.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("setdate") && sender.hasPermission("calendar.setdate")) {
            if (args.length != 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /setdate <year> <month> <day>");
                return false;
            }

            try {
                int year = Integer.parseInt(args[0]);
                int month = Integer.parseInt(args[1]);
                int day = Integer.parseInt(args[2]);

                LocalDate newDate = LocalDate.of(year, month, day);

                for (World world : plugin.getServer().getWorlds()) {
                    worldDates.put(world.getName(), newDate);
                }

                sender.sendMessage(ChatColor.GREEN + "Date set to " + newDate.toString() + " for all worlds.");
                return true;

            } catch (NumberFormatException | DateTimeException e) {
                sender.sendMessage(ChatColor.RED + "Invalid date. Make sure to input valid numbers.");
                return false;
            }
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("temperature")) {
            if (args.length == 1) {
                return Arrays.asList("set");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
                return Arrays.asList("celsius", "fahrenheit");
            }
        } else if (command.getName().equalsIgnoreCase("date")) {
            if (args.length == 1) {
                return Arrays.asList("format", "time","reload");
            } else if (args.length == 2 && args[0].equals("format")) {
                return Arrays.asList("dd/MM/yyy", "MM/dd/yyy", "yyy-MM-dd");
            } else if (args.length == 2 && args[0].equals("time")) {
                return Arrays.asList("12", "24");
            }
        }

        return null;
    }

}
