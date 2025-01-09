package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.CTrait;

import java.util.HashMap;
import java.util.Map;

public class MagicCTrait extends CTrait {
    public MagicCTrait() {
        super("No Magic", 6);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
