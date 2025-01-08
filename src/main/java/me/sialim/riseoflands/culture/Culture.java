package me.sialim.riseoflands.culture;

import java.sql.Array;
import java.util.List;
import java.util.ArrayList;

public class Culture {
    private final String name;
    private final String owner;
    private final List<String> members;

    public Culture(String name, String owner, List<String> members) {
        this.name = name;
        this.owner = owner;
        this.members = new ArrayList<>(members);
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public List<String> getMembers() {
        return members;
    }

    public void addMember(String playerName) {
        if (!members.contains(playerName)) {
            members.add(playerName);
        }
    }

    public void removeMember(String playerName) {
        members.remove(playerName);
    }
}
