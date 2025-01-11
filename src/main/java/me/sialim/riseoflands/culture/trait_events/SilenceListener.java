package me.sialim.riseoflands.culture.trait_events;

import me.sialim.riseoflands.RiseOfLands;
import me.sialim.riseoflands.culture.RTrait;
import me.sialim.riseoflands.culture.ReligionCooldown;
import me.sialim.riseoflands.culture.ReligionManager;
import me.sialim.riseoflands.culture.traits.SilenceCTrait;
import me.sialim.riseoflands.government.ReputationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SilenceListener implements Listener {
    private final RiseOfLands plugin;

    public SilenceListener(RiseOfLands plugin) { this.plugin = plugin; }

    @EventHandler public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        if (plugin.religionManager.getPlayerCulture(uuid) == null) return;

        if (hasSilenceTrait(uuid)) {
            RTrait silenceTrait = new SilenceCTrait();
            plugin.religionManager.handleTraitViolation(uuid, silenceTrait, silenceTrait.getPoints());
        }
    }

    private boolean hasSilenceTrait(UUID uuid) {
        return plugin.religionManager.getPlayerCulture(uuid).getTraits()
                .stream().anyMatch(trait -> trait.getName().equals("Silence"));
    }
}
