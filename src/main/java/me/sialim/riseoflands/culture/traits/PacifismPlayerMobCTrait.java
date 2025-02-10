package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.RTrait;
import org.bukkit.entity.EntityType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PacifismPlayerMobCTrait extends RTrait {
    public static final Set<EntityType> restricted = EnumSet.of(
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER, EntityType.ENDERMAN,
            EntityType.WITCH, EntityType.WITHER_SKELETON, EntityType.BLAZE, EntityType.GHAST, EntityType.ZOMBIFIED_PIGLIN,
            EntityType.PHANTOM, EntityType.DROWNED, EntityType.STRAY, EntityType.PILLAGER, EntityType.VINDICATOR,
            EntityType.EVOKER, EntityType.RAVAGER, EntityType.HOGLIN, EntityType.PIGLIN_BRUTE, EntityType.WARDEN,
            EntityType.PLAYER
    );
    public PacifismPlayerMobCTrait() {
        super("Pacifism to Players & Mobs", 6);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
