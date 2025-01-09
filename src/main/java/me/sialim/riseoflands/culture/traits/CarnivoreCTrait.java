package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.CTrait;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarnivoreCTrait extends CTrait {
    private static final List<Material> restricted = Arrays.asList(
            Material.APPLE, Material.CARROT, Material.POTATO,
            Material.BAKED_POTATO, Material.BREAD, Material.MELON_SLICE,
            Material.SWEET_BERRIES, Material.PUMPKIN_PIE, Material.MUSHROOM_STEW,
            Material.BEETROOT, Material.BEETROOT_SOUP, Material.CAKE,
            Material.COOKIE
    );
    public CarnivoreCTrait() {
        super("Carnivore", 5);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
