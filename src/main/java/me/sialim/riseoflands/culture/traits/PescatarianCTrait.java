package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.DietRTrait;
import me.sialim.riseoflands.culture.RTrait;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PescatarianCTrait extends DietRTrait {
    public PescatarianCTrait() {
        super("Pescatarian", 3, Arrays.asList(
                Material.COOKED_BEEF, Material.COOKED_PORKCHOP, Material.COOKED_MUTTON,
                Material.COOKED_CHICKEN, Material.COOKED_RABBIT, Material.CHICKEN,
                Material.PORKCHOP, Material.BEEF, Material.MUTTON, Material.ROTTEN_FLESH
        ));
    }
}
