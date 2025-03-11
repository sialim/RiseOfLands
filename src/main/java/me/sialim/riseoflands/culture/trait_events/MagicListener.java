package me.sialim.riseoflands.culture.trait_events;

import me.sialim.riseoflands.RiseOfLandsMain;
import me.sialim.riseoflands.culture.RTrait;
import me.sialim.riseoflands.culture.traits.MagicCTrait;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class MagicListener implements Listener {
    private final RiseOfLandsMain plugin;

    private final Set<PotionType> nonMagicalPotions = EnumSet.of(
            PotionType.WATER, PotionType.AWKWARD, PotionType.THICK, PotionType.MUNDANE
    );

    public MagicListener(RiseOfLandsMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler public void onPotionUse(PlayerItemConsumeEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        ItemStack item = e.getItem();
        if (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) {
            if (isNonMagicalPotion(item)) return;
            checkForMagic(uuid);
        }
    }

    @EventHandler public void onPotionThrow(PotionSplashEvent e) {
        if (e.getEntity().getShooter() instanceof Player p) {
            UUID uuid = p.getUniqueId();
            ItemStack item = e.getPotion().getItem();
            if (isNonMagicalPotion(item)) return;
            checkForMagic(uuid);
        }
    }

    @EventHandler public void onWeaponUse(PlayerInteractEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        ItemStack item = e.getItem();
        if (item != null && item.hasItemMeta()) {
            if (item.getItemMeta().hasEnchants()) checkForMagic(uuid);
        }
    }

    @EventHandler public void onBowShoot(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player p) {
            UUID uuid = p.getUniqueId();
            ItemStack item = e.getBow();
            if (item != null && item.hasItemMeta()) {
                if (item.getItemMeta().hasEnchants()) checkForMagic(uuid);
            }
        }
    }

    @EventHandler public void onEnchant(EnchantItemEvent e) {
        UUID uuid = e.getEnchanter().getUniqueId();
        checkForMagic(uuid);
    }

    @EventHandler public void onArmorEquip(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            UUID uuid = p.getUniqueId();
            int rawSlot = e.getRawSlot();
            if (isArmorSlot(rawSlot)) {
                ItemStack clickedItem = e.getCursor();
                if (clickedItem != null && clickedItem.hasItemMeta()) {
                    if (clickedItem.getItemMeta().hasEnchants()) {
                        checkForMagic(uuid);
                    }
                }
            }
        }
    }

    private void checkForMagic(UUID uuid) {
        if (hasMagicTrait(uuid)) {
            RTrait magicTrait = new MagicCTrait();
            plugin.religionManager.handleTraitViolation(uuid, magicTrait, magicTrait.getPoints());
        }
    }

    private boolean hasMagicTrait(UUID uuid) {
        if (plugin.religionManager.getPlayerCulture(uuid) == null) {
            return false;
        }
        return plugin.religionManager.getPlayerCulture(uuid).getTraits()
                .stream().anyMatch(trait -> trait.getName().equals("No Magic"));
    }

    private boolean isArmorSlot(int slot) {
        return slot >= 100 && slot <= 103;
    }

    private boolean isNonMagicalPotion(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        if(!(item.getItemMeta() instanceof PotionMeta potionMeta)) return false;
        PotionType potionType = potionMeta.getBasePotionType();
        return nonMagicalPotions.contains(potionType);
    }
}
