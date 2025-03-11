package me.sialim.riseoflands.roleplay;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.sialim.riseoflands.RiseOfLandsMain;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class IdentityPlaceholder extends PlaceholderExpansion {
    private final RiseOfLandsMain plugin;

    public IdentityPlaceholder(RiseOfLandsMain plugin) { this.plugin = plugin; }

    @Override public boolean persist() { return true; }
    @Override public boolean canRegister() { return true; }

    @Override public @NotNull String getIdentifier() { return "id"; }

    @Override public @NotNull String getAuthor() { return "bermei"; }

    @Override public @NotNull String getVersion() { return "1.0"; }

    @Override public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";
        UUID uuid = player.getUniqueId();
        if (identifier.equals("name")) return plugin.identityManager.getRoleplayName(uuid);
        if (identifier.equals("gender")) return plugin.identityManager.getGenderDisplay(uuid);
        if (identifier.equals("displaylabel")) return plugin.identityManager.getDisplayLabel(player);
        return null;
    }
}