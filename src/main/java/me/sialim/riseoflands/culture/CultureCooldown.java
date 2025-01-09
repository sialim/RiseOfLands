package me.sialim.riseoflands.culture;

import java.util.Set;

public class CultureCooldown {
    private long cultureCreateTime = 0;
    private long cultureJoinTime = 0;
    private long cultureLeaveTime = 0;
    private long cultureDeleteTime = 0;
    private Set<String> brokenTraits;
    private long forgivenessTimer = 0;
    private boolean reputationReset = false;

    public long getCultureCreateTime() { return cultureCreateTime; }

    public long getCultureJoinTime() { return cultureJoinTime; }

    public long getCultureLeaveTime() { return cultureLeaveTime; }

    public long getCultureDeleteTime() { return cultureDeleteTime; }

    public Set<String> getBrokenTraits() { return brokenTraits; }

    public long getForgivenessTimer() { return forgivenessTimer; }

    public boolean getReputationReset() { return reputationReset; }

    public void setCultureCreateTime(long cultureCreateTime) { this.cultureCreateTime = cultureCreateTime; }

    public void setCultureJoinTime(long cultureJoinTime) { this.cultureJoinTime = cultureJoinTime; }

    public void setCultureLeaveTime(long cultureLeaveTime) { this.cultureLeaveTime = cultureLeaveTime; }

    public void setCultureDeleteTime(long cultureDeleteTime) { this.cultureDeleteTime = cultureDeleteTime; }

    public void setBrokenTraits(Set<String> brokenTraits) { this.brokenTraits = brokenTraits; }

    public void setForgivenessTimer(long forgivenessTimer) { this.forgivenessTimer = forgivenessTimer; }

    public void setReputationReset(boolean reputationReset) { this.reputationReset = reputationReset; }

    public void clearBrokenTraits() { brokenTraits.clear(); }
}