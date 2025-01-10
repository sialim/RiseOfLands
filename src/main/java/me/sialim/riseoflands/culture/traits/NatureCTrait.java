package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.RTrait;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NatureCTrait extends RTrait {
    public final List<Material> restricted = Arrays.asList(
            Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES,
            Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES,
            Material.MANGROVE_LEAVES, Material.CHERRY_LEAVES, Material.NETHER_WART_BLOCK,
            Material.WARPED_WART_BLOCK, Material.OAK_LOG, Material.SPRUCE_LOG,
            Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.ACACIA_LOG,
            Material.DARK_OAK_LOG, Material.MANGROVE_LOG, Material.CHERRY_LOG,
            Material.CRIMSON_STEM, Material.WARPED_STEM, Material.BAMBOO
    );
    public NatureCTrait() {
        super("Nature", 5);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
