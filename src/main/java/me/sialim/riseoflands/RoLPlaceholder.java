package me.sialim.riseoflands;

import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.player.LandPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RoLPlaceholder extends PlaceholderExpansion {
    private final RiseOfLands plugin;

    public RoLPlaceholder(RiseOfLands plugin) { this.plugin = plugin; }

    @Override public boolean persist() { return true; }
    @Override public boolean canRegister() { return true; }

    @Override public @NotNull String getIdentifier() { return "rol"; }

    @Override public @NotNull String getAuthor() { return "bermei"; }

    @Override public @NotNull String getVersion() { return "1.0"; }

    @Override public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";
        UUID uuid = player.getUniqueId();
        if (identifier.equals("rep")) return String
                .valueOf(plugin.reputationManager
                        .getPlayerReputation(uuid));

        if (identifier.equals("land_rep")) {
            LandPlayer landPlayer = plugin.api.getLandPlayer(uuid);
            Land land = landPlayer.getEditLand();
            return land != null ? String.valueOf(plugin.reputationManager.getLandReputation(land)) : "None";
        }

        return null;
    }
}
