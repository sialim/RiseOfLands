package me.sialim.riseoflands.culture;

import java.sql.Array;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class Culture {
    private final String name;
    private final UUID owner;
    private final List<UUID> members;

    public Culture(String name, UUID owner, List<UUID> members) {
        this.name = name;
        this.owner = owner;
        this.members = new ArrayList<>(members);
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID playerName) {
        if (!members.contains(playerName)) {
            members.add(playerName);
        }
    }

    public void removeMember(UUID playerName) {
        members.remove(playerName);
    }
}
