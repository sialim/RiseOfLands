package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.CTrait;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class PacifismPlayerCTrait extends CTrait {
    public static final EntityType restricted = EntityType.PLAYER;

    public PacifismPlayerCTrait() {
        super("Pacifism to Players", 3);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
