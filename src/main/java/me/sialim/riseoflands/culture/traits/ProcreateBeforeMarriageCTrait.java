package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.CTrait;

import java.util.HashMap;
import java.util.Map;

public class ProcreateBeforeMarriageCTrait extends CTrait {
    public ProcreateBeforeMarriageCTrait() {
        super("No Procreation before Marriage", 3);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
