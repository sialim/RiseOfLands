package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.HolyMobRTrait;
import me.sialim.riseoflands.culture.RTrait;
import org.bukkit.entity.EntityType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NeutralMobCTrait extends HolyMobRTrait {
    public final Set<EntityType> neutralMobs = EnumSet.of(
            EntityType.WOLF, EntityType.BEE, EntityType.PIGLIN, EntityType.LLAMA, EntityType.PANDA,
            EntityType.DOLPHIN, EntityType.GOAT, EntityType.IRON_GOLEM
    );
    public EntityType restricted;
    public NeutralMobCTrait(EntityType restricted) {
        super("Neutral Mob", 2, restricted);
    }
}
