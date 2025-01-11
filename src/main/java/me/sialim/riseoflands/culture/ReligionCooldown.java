package me.sialim.riseoflands.culture;

import java.util.*;

public class ReligionCooldown {
    private long cultureCreateTime = 0;
    private long cultureJoinTime = 0;
    private long cultureLeaveTime = 0;
    private long cultureDeleteTime = 0;
    private Set<RTrait> brokenTraits = new HashSet<>();
    private long forgivenessTimer = 0;
    private boolean reputationReset = false;

    public long getCultureCreateTime() { return cultureCreateTime; }

    public long getCultureJoinTime() { return cultureJoinTime; }

    public long getCultureLeaveTime() { return cultureLeaveTime; }

    public long getCultureDeleteTime() { return cultureDeleteTime; }

    public Set<RTrait> getBrokenTraits() {
        if (brokenTraits == null) {
            brokenTraits = new HashSet<>();  // Return an empty set instead of null
        }
        return brokenTraits;
    }

    public long getForgivenessTimer() { return forgivenessTimer; }

    public boolean getReputationReset() { return reputationReset; }

    public void setCultureCreateTime(long cultureCreateTime) { this.cultureCreateTime = cultureCreateTime; }

    public void setCultureJoinTime(long cultureJoinTime) { this.cultureJoinTime = cultureJoinTime; }

    public void setCultureLeaveTime(long cultureLeaveTime) { this.cultureLeaveTime = cultureLeaveTime; }

    public void setCultureDeleteTime(long cultureDeleteTime) { this.cultureDeleteTime = cultureDeleteTime; }

    public void setBrokenTraits(Set<RTrait> brokenTraits) { this.brokenTraits = brokenTraits; }

    public void setForgivenessTimer(long forgivenessTimer) { this.forgivenessTimer = forgivenessTimer; }

    public void setReputationReset(boolean reputationReset) { this.reputationReset = reputationReset; }

    public void clearBrokenTraits() { brokenTraits.clear(); }

    @Deprecated public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("cultureCreateTime", cultureCreateTime);
        map.put("cultureJoinTime", cultureJoinTime);
        map.put("cultureLeaveTime", cultureLeaveTime);
        map.put("cultureDeleteTime", cultureDeleteTime);
        map.put("forgivenessTimer", forgivenessTimer);
        map.put("reputationReset", reputationReset);

        List<Map<String, Object>> brokenTraitsList = new ArrayList<>();
        for (RTrait trait : brokenTraits) {
            brokenTraitsList.add(trait.toMap());
        }
        map.put("brokenTraits", brokenTraitsList);

        return map;
    }

    @Deprecated public static ReligionCooldown fromMap(Map<String, Object> map) {
        ReligionCooldown cooldown = new ReligionCooldown();
        cooldown.setCultureCreateTime((long) map.get("cultureCreateTime"));
        cooldown.setCultureJoinTime((long) map.get("cultureJoinTime"));
        cooldown.setCultureLeaveTime((long) map.get("cultureLeaveTime"));
        cooldown.setCultureDeleteTime((long) map.get("cultureDeleteTime"));
        cooldown.setForgivenessTimer((long) map.get("forgivenessTimer"));
        cooldown.setReputationReset((boolean) map.get("reputationReset"));

        List<Map<String, Object>> brokenTraitsList = (List<Map<String, Object>>) map.get("brokenTraits");
        Set<RTrait> brokenTraitsSet = new HashSet<>();
        for (Map<String, Object> traitMap : brokenTraitsList) {
            RTrait trait = RTrait.fromMap(traitMap);
            brokenTraitsSet.add(trait);
        }
        cooldown.setBrokenTraits(brokenTraitsSet);
        return cooldown;
    }
}