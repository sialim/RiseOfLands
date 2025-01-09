package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.CTrait;
import org.bukkit.entity.EntityType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HostileMobCTrait extends CTrait {
    public static final Set<EntityType> hostileMobs = EnumSet.of(
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER, EntityType.ENDERMAN,
            EntityType.WITCH, EntityType.WITHER_SKELETON, EntityType.BLAZE, EntityType.GHAST, EntityType.ZOMBIFIED_PIGLIN,
            EntityType.PHANTOM, EntityType.DROWNED, EntityType.STRAY, EntityType.PILLAGER, EntityType.VINDICATOR,
            EntityType.EVOKER, EntityType.RAVAGER, EntityType.HOGLIN, EntityType.PIGLIN_BRUTE, EntityType.WARDEN
    );
    public EntityType restricted;
    public HostileMobCTrait(EntityType restricted) {
        super("Hostile Mob", 3);
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
