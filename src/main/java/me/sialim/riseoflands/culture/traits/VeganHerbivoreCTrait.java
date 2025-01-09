package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.CTrait;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

public class VeganHerbivoreCTrait extends CTrait {
    private static final List<Material> restricted = Arrays.asList(
            Material.COOKED_BEEF, Material.COOKED_PORKCHOP, Material.COOKED_MUTTON,
            Material.COOKED_CHICKEN, Material.COOKED_RABBIT, Material.EGG,
            Material.MILK_BUCKET, Material.HONEY_BOTTLE, Material.BEEF,
            Material.PORKCHOP, Material.MUTTON, Material.CHICKEN,
            Material.RABBIT, Material.ROTTEN_FLESH, Material.COD,
            Material.COOKED_COD, Material.SALMON, Material.COOKED_SALMON,
            Material.CAKE
    );
    public VeganHerbivoreCTrait() {
        super("Vegan/Herbivore", 4);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
