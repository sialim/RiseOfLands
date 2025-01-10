package me.sialim.riseoflands.culture.traits;

import me.sialim.riseoflands.culture.RTrait;

import java.util.HashMap;
import java.util.Map;

public class SilenceCTrait extends RTrait {
    public SilenceCTrait() {
        super("Silence", 7);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("traitName", super.getName());
        return map;
    }
}
