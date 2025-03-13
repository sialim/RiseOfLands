package me.sialim.riseoflands.roleplay;

import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.player.LandPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.sialim.riseoflands.RiseOfLandsMain;
import org.bukkit.ChatColor;
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
        String fallback = ChatColor.GRAY + "None";
        if (!plugin.identityManager.hasIdentity(player.getUniqueId())) return fallback;
        UUID uuid = player.getUniqueId();
        if (identifier.equals("name")) return plugin.identityManager.getRoleplayName(uuid);
        if (identifier.equals("gender")) return plugin.identityManager.getGenderDisplay(uuid);
        if (identifier.equals("displaylabel")) return plugin.identityManager.getDisplayLabel(player);
        if (identifier.equals("loyaltylabel")) {
            if (plugin.api.getLandPlayer(uuid) != null) {
                LandPlayer landPlayer = plugin.api.getLandPlayer(uuid);
                Land land = landPlayer.getEditLand(false);
                if (land == null) return fallback;
                if (plugin.identityManager.isShowingNation(plugin.identityManager.getIdentity(uuid))) {
                    return land.getNation() != null ? land.getNation().getColorName() : land.getColorName();
                } else {
                    return land.getColorName();
                }
            } else {
                return fallback;
            }
        }
        if (identifier.equals("rank")) return plugin.identityManager
                .getRankLabel(plugin.identityManager.getPlayerRank(player), plugin.identityManager.getIdentity(uuid));
        return null;
    }
}