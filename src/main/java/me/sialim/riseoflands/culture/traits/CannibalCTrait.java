package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.DietRTrait;
import me.sialim.riseoflands.culture.RTrait;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CannibalCTrait extends DietRTrait {
    public CannibalCTrait() {
        super("Cannibal", 6, Arrays.asList(
                Material.APPLE, Material.CARROT, Material.POTATO,
                Material.BAKED_POTATO, Material.BREAD, Material.MELON_SLICE,
                Material.SWEET_BERRIES, Material.PUMPKIN_PIE, Material.MUSHROOM_STEW,
                Material.BEETROOT, Material.BEETROOT_SOUP, Material.COOKED_BEEF,
                Material.COOKED_SALMON, Material.COOKED_COD, Material.COOKED_RABBIT,
                Material.COOKED_CHICKEN, Material.COOKED_MUTTON, Material.MUTTON,
                Material.CHICKEN, Material.RABBIT, Material.COD,
                Material.SALMON, Material.BEEF, Material.CAKE,
                Material.COOKIE, Material.GOLDEN_CARROT, Material.SUSPICIOUS_STEW
        ));
    }
}
