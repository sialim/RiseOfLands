package me.sialim.riseoflands.culture.trait_events;

import me.sialim.riseoflands.RiseOfLandsMain;
import me.sialim.riseoflands.culture.RTrait;
import me.sialim.riseoflands.culture.traits.SilenceCTrait;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class SilenceListener implements Listener {
    private final RiseOfLandsMain plugin;

    public SilenceListener(RiseOfLandsMain plugin) { this.plugin = plugin; }

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
        if (plugin.religionManager.getPlayerCulture(uuid) == null) {
            return false;
        }
        return plugin.religionManager.getPlayerCulture(uuid).getTraits()
                .stream().anyMatch(trait -> trait.getName().equals("Silence"));
    }
}
