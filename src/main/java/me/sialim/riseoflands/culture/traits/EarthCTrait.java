package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.RTrait;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EarthCTrait extends RTrait {
    private final List<Material> restricted = Arrays.asList(
            Material.STONE, Material.GRANITE, Material.ANDESITE,
            Material.DIORITE
    );
    public EarthCTrait() {
        super("Earth", 6);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
