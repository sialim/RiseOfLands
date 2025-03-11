package me.sialim.riseoflands.calendar;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.sialim.riseoflands.RiseOfLandsMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.UUID;

public class CalendarPlaceholder extends PlaceholderExpansion {
    private RiseOfLandsMain plugin;

    public CalendarPlaceholder(RiseOfLandsMain plugin) { this.plugin = plugin; }
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "calendar";
    }

    @Override
    public @NotNull String getAuthor() {
        return "bermei";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        String fallback = ChatColor.GRAY + "None";
        PlayerDataManager.PlayerPreferences preferences = plugin.calendar.playerDataManager.getPlayerPreferences(player.getUniqueId());
        if (identifier.startsWith("formatted_date_world_")) {
            String worldName = identifier.substring("formatted_date_world_".length());
            World world = Bukkit.getServer().getWorld(worldName);
            if (world != null) {
                LocalDate date = plugin.calendar.worldDates.get(plugin.getConfig().getString("main-world"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(preferences.getDateFormat());
                return plugin.calendar.getFormattedDate(world);
            }
        } else if (identifier.startsWith("formatted_time_world")) {
            String worldName = identifier.substring("formatted_date_world_".length());
            World world = Bukkit.getServer().getWorld(worldName);
            if (world != null) {
                return plugin.calendar.getFormattedTime(world, player);
            }
        } else if (identifier.equals("temperature")) {
            int temperature = plugin.asAPI.getTemperature(player);
            String temperatureUnit = preferences.getTemperatureUnit();
            Bukkit.getLogger().info("Fetched temperature: " + temperature + " for player: " + player.getName());
            if ("fahrenheit".equalsIgnoreCase(temperatureUnit)) {
                temperature = (temperature * 9 / 5) + 32;
            }
            return temperature + "*" + (temperatureUnit.equalsIgnoreCase("fahrenheit") ? " Fahrenheit" : " Celsius");
        } else if (identifier.equals("localized_time")) {
            String format = preferences.getDateFormat().replace("DD","dd");
            format = format.replace("mm", "MM");
            if (format.isEmpty()) return "Invalid date format";

            try {
                World world = player.getWorld();
                LocalDate date = plugin.calendar.worldDates.get(plugin.getConfig().getString("main-world"));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

                return date.format(formatter);
            } catch (IllegalArgumentException | UnsupportedTemporalTypeException e) {
                return "Invalid date format pattern";
            }
        } else if (identifier.equals("age")) {
            UUID uuid = player.getUniqueId();
            return plugin.identityManager.calculateAge(uuid);
        } else if (identifier.equals("birthday")) {
            UUID uuid = player.getUniqueId();
            LocalDate birthdate = plugin.identityManager.getBirthDate(uuid);
            String format = preferences.getDateFormat().replace("DD","dd");
            format = format.replace("mm", "MM");
            if (format.isEmpty()) return "Invalid date format";
            try {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

                return birthdate.format(formatter);
            } catch (IllegalArgumentException | UnsupportedTemporalTypeException e) {
                return "Invalid date format pattern";
            }
        } else if (identifier.equals("season")) {
            String season = plugin.asAPI.getSeason(Bukkit.getWorld(plugin.getConfig().getString("main-world")));
            String[] message = season.split(" ");
            return Character.toUpperCase(message[0].charAt(0)) + message[0].substring(1).toLowerCase();
        }
        return fallback;
    }
}
