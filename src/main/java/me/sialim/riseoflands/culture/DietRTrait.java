package me.sialim.riseoflands.culture;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public abstract class DietRTrait extends RTrait {
    public final List<Material> restricted;
    public DietRTrait(String name, int points, List<Material> restricted) {
        super(name, points);
        this.restricted = restricted;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
