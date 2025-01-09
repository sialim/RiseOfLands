package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.CTrait;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PescatarianCTrait extends CTrait {
    private static final List<Material> restricted = Arrays.asList(
            Material.COOKED_BEEF, Material.COOKED_PORKCHOP, Material.COOKED_MUTTON,
            Material.COOKED_CHICKEN, Material.COOKED_RABBIT, Material.CHICKEN,
            Material.PORKCHOP, Material.BEEF, Material.MUTTON
    );
    public PescatarianCTrait() {
        super("Pescatarian", 3);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
