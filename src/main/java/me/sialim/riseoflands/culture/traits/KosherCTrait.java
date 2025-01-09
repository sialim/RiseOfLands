package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.CTrait;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KosherCTrait extends CTrait {
    private static final List<Material> restricted = Arrays.asList(
            Material.PORKCHOP, Material.COOKED_PORKCHOP, Material.BEEF,
            Material.COOKED_BEEF, Material.RABBIT, Material.COOKED_RABBIT
    );
    public KosherCTrait() {
        super("Kosher", 2);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
