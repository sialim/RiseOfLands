package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.CTrait;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CannibalCTrait extends CTrait {
    private static final List<Material> restricted = Arrays.asList(
            Material.APPLE, Material.CARROT, Material.POTATO,
            Material.BAKED_POTATO, Material.BREAD, Material.MELON_SLICE,
            Material.SWEET_BERRIES, Material.PUMPKIN_PIE, Material.MUSHROOM_STEW,
            Material.BEETROOT, Material.BEETROOT_SOUP, Material.COOKED_BEEF,
            Material.COOKED_SALMON, Material.COOKED_COD, Material.COOKED_RABBIT,
            Material.COOKED_CHICKEN, Material.COOKED_MUTTON, Material.MUTTON,
            Material.CHICKEN, Material.RABBIT, Material.COD,
            Material.SALMON, Material.BEEF, Material.CAKE,
            Material.COOKIE
    );
    public CannibalCTrait() {
        super("Cannibal", 6);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
