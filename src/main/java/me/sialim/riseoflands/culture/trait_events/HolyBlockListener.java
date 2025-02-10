package me.sialim.riseoflands.culture.trait_events;

import me.sialim.riseoflands.RiseOfLands;
import me.sialim.riseoflands.culture.RTrait;
import me.sialim.riseoflands.culture.traits.EarthCTrait;
import me.sialim.riseoflands.culture.traits.NatureCTrait;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class HolyBlockListener implements Listener {
    private final RiseOfLands plugin;

    @EventHandler public void onBlockBreak(BlockBreakEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Material blockType = e.getBlock().getType();
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();

        if (item != null && item.hasItemMeta() && item.getItemMeta().hasEnchants() &&
                item.getItemMeta().getEnchants().containsKey(Enchantment.SILK_TOUCH)) {
            return;
        }


        if (plugin.religionManager.getPlayerCulture(uuid) == null) {
            return;
        }

        if(hasEarthTrait(uuid) && isEarthBlock(blockType)) checkForEarth(uuid);

        if(hasNatureTrait(uuid) && isNatureBlock(blockType)) checkForNature(uuid);
    }

    public HolyBlockListener(RiseOfLands plugin) {
        this.plugin = plugin;
    }

    private void checkForEarth(UUID uuid) {
        RTrait earthTrait = new EarthCTrait();
        plugin.religionManager.handleTraitViolation(uuid, earthTrait, earthTrait.getPoints());
    }

    private boolean hasEarthTrait(UUID uuid) {
        return plugin.religionManager.getPlayerCulture(uuid).getTraits()
                .stream().anyMatch(trait -> trait.getName().equals("Earth"));
    }

    private boolean isEarthBlock(Material material) {
        return EarthCTrait.restricted.contains(material);
    }

    private void checkForNature(UUID uuid) {
        RTrait natureTrait = new NatureCTrait();
        plugin.religionManager.handleTraitViolation(uuid, natureTrait, natureTrait.getPoints());
    }

    private boolean hasNatureTrait(UUID uuid) {
        return plugin.religionManager.getPlayerCulture(uuid).getTraits()
                .stream().anyMatch(trait -> trait.getName().equals("Nature"));
    }

    private boolean isNatureBlock(Material material) {
        return NatureCTrait.restricted.contains(material);
    }
}