package me.sialim.riseoflands.culture.trait_events;

import me.sialim.riseoflands.RiseOfLands;
import me.sialim.riseoflands.culture.RTrait;
import me.sialim.riseoflands.culture.traits.EarthCTrait;
import me.sialim.riseoflands.culture.traits.RedstoneCTrait;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class RedstoneListener implements Listener {
    public RiseOfLands plugin;

    public RedstoneListener(RiseOfLands plugin) { this.plugin = plugin; }

    @EventHandler public void onBlockPlace(BlockPlaceEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Material blockType = e.getBlock().getType();

        if (hasRedstoneTrait(uuid))
            if (RedstoneCTrait.restricted.contains(blockType)) checkForRedstone(uuid);
    }

    @EventHandler public void onPlayerInteract(PlayerInteractEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Material blockType = e.getClickedBlock() != null ? e.getClickedBlock().getType() : null;

        if (hasRedstoneTrait(uuid) && blockType != null && isRestrictedInteraction(e.getAction(), blockType))
            checkForRedstone(uuid);
    }

    private void checkForRedstone(UUID uuid) {
        RTrait redstoneTrait = new RedstoneCTrait();
        plugin.religionManager.handleTraitViolation(uuid, redstoneTrait, redstoneTrait.getPoints());
    }

    private boolean hasRedstoneTrait(UUID uuid) {
        return plugin.religionManager.getPlayerCulture(uuid).getTraits()
                .stream().anyMatch(trait -> trait.getName().equals("No Redstone"));
    }

    private boolean isRestrictedInteraction(Action action, Material material) {
        return (action == Action.RIGHT_CLICK_BLOCK || action == Action.PHYSICAL) &&
                RedstoneCTrait.interactable.contains(material);
    }
}
