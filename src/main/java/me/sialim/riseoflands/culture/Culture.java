package me.sialim.riseoflands.culture;

import java.util.*;
import java.util.stream.Collectors;

public class Culture {
    private final String name;
    private final UUID owner;
    private final List<UUID> members;
    private final List<CTrait> traits;

    public Culture(String name, UUID owner, List<UUID> members, List<CTrait> traits) {
        this.name = name;
        this.owner = owner;
        this.members = new ArrayList<>(members);
        this.traits = traits;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("owner", owner.toString());
        map.put("members", members.stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("traits", traits.stream().map(CTrait::toMap).collect(Collectors.toList()));
        return map;
    }

    public static Culture fromMap(Map<String, Object> map) {
        String name = (String) map.get("name");
        UUID owner = UUID.fromString((String) map.get("owner"));
        List<UUID> members = ((List<String>) map.get("members")).stream().map(UUID::fromString).collect(Collectors.toList());
        List<CTrait> traits = ((List<Map<String, Object>>) map.get("traits")).stream()
                .map(CTrait::fromMap).collect(Collectors.toList());
        return new Culture(name, owner, members, traits);
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

    public List<CTrait> getTraits() { return traits; }

    public void addMember(UUID playerName) {
        if (!members.contains(playerName)) {
            members.add(playerName);
        }
    }

    public void removeMember(UUID playerName) {
        members.remove(playerName);
    }
}
