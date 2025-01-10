package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.DietRTrait;
import me.sialim.riseoflands.culture.RTrait;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KosherCTrait extends DietRTrait {
    public KosherCTrait() {
        super("Kosher", 2, Arrays.asList(
                Material.PORKCHOP, Material.COOKED_PORKCHOP, Material.BEEF,
                Material.COOKED_BEEF, Material.RABBIT, Material.COOKED_RABBIT
        ));
    }
}
