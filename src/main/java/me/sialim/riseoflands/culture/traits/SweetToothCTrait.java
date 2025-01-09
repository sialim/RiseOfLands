package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.CTrait;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SweetToothCTrait extends CTrait {
    private static final List<Material> restricted = Arrays.asList(
            Material.APPLE, Material.CARROT, Material.POTATO,
            Material.BAKED_POTATO, Material.BREAD, Material.MELON_SLICE,
            Material.MUSHROOM_STEW, Material.SALMON, Material.BEEF,
            Material.BEETROOT, Material.BEETROOT_SOUP, Material.COOKED_BEEF,
            Material.COOKED_SALMON, Material.COOKED_COD, Material.COOKED_RABBIT,
            Material.COOKED_CHICKEN, Material.COOKED_MUTTON, Material.MUTTON,
            Material.CHICKEN, Material.RABBIT, Material.COD
    );
    public SweetToothCTrait() {
        super("Sweet Tooth", 4);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
