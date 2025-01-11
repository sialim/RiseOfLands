package me.sialim.riseoflands.culture;

import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class HolyMobRTrait extends RTrait{
    public EntityType restricted;
    public HolyMobRTrait(String name, int points, EntityType restricted) {
        super(name, points);
        this.restricted = restricted;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        map.put("mobType", restricted.name());
        return map;
    }
}
