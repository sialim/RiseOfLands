package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.RTrait;

import java.util.HashMap;
import java.util.Map;

public class ProcreateBeforeMarriageCTrait extends RTrait {
    public ProcreateBeforeMarriageCTrait() {
        super("Chaste", 3);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
