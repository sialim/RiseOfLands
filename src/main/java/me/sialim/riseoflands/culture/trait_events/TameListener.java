package me.sialim.riseoflands.culture.trait_events;

import me.sialim.riseoflands.RiseOfLands;
import me.sialim.riseoflands.culture.RTrait;
import me.sialim.riseoflands.culture.traits.RedstoneCTrait;
import me.sialim.riseoflands.culture.traits.TamingCTrait;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

import java.util.UUID;

public class TameListener implements Listener {
    public RiseOfLands plugin;

    public TameListener(RiseOfLands plugin) { this.plugin = plugin; }

    @EventHandler public void onEntityTame(EntityTameEvent e) {
        UUID uuid = e.getOwner().getUniqueId();

        if (hasTamingTrait(uuid))
            checkForTaming(uuid);
    }

    private void checkForTaming(UUID uuid) {
        RTrait tamingTrait = new TamingCTrait();
        plugin.religionManager.handleTraitViolation(uuid, tamingTrait, tamingTrait.getPoints());
    }

    private boolean hasTamingTrait(UUID uuid) {
        return plugin.religionManager.getPlayerCulture(uuid).getTraits()
                .stream().anyMatch(trait -> trait.getName().equals("No Taming"));
    }

}
