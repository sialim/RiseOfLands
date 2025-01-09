package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.CTrait;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedstoneCTrait extends CTrait {
    private static final List<Material> restricted = Arrays.asList(
            Material.REDSTONE, Material.REDSTONE_LAMP, Material.LEVER,
            Material.STONE_BUTTON, Material.OAK_BUTTON, Material.SPRUCE_BUTTON,
            Material.BIRCH_BUTTON, Material.JUNGLE_BUTTON, Material.ACACIA_BUTTON,
            Material.DARK_OAK_BUTTON, Material.CRIMSON_BUTTON, Material.WARPED_BUTTON,
            Material.POLISHED_BLACKSTONE_BUTTON, Material.STONE_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.OAK_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE, Material.ACACIA_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE, Material.WARPED_PRESSURE_PLATE,
            Material.BAMBOO_BUTTON, Material.BAMBOO_PRESSURE_PLATE
    );
    public RedstoneCTrait() {
        super("No Redstone", 4);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
