package me.sialim.riseoflands.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import me.sialim.riseoflands.RiseOfLandsMain;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatFormatter implements Listener {
    private final RiseOfLandsMain plugin;
    private String chatFormat;
    public ChatFormatter(RiseOfLandsMain plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        chatFormat = plugin.getConfig().getString("chat-format", "&7[&a%player_name%&7] &f%message%");
        chatFormat = ChatColor.translateAlternateColorCodes('&', chatFormat);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();
        String formattedChat = PlaceholderAPI.setPlaceholders(player, chatFormat);

        formattedChat = formattedChat.replace("%message%", message);
        e.setFormat(formattedChat);
    }
}
