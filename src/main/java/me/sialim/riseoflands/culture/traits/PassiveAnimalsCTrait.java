package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.CTrait;
import org.bukkit.entity.EntityType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PassiveAnimalsCTrait extends CTrait {
    public static final Set<EntityType> restricted = EnumSet.of(
            EntityType.COW, EntityType.SHEEP, EntityType.CHICKEN, EntityType.PIG, EntityType.RABBIT,
            EntityType.HORSE, EntityType.DONKEY, EntityType.MULE, EntityType.CAT, EntityType.PARROT,
            EntityType.FOX, EntityType.TURTLE, EntityType.AXOLOTL, EntityType.VILLAGER
    );
    public PassiveAnimalsCTrait() {
        super("Passive Animals", 3);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
