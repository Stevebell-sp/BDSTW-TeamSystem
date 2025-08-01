package org.bdstw.teamsystem.team;

import net.minecraft.ChatFormatting;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Team {
    private final String name;
    private UUID leader;
    private final Set<UUID> members;
    private final ChatFormatting color;

    // 修正：建構子不再需要 password 參數
    public Team(String name, UUID leader, ChatFormatting color) {
        this.name = name;
        this.leader = leader;
        this.members = ConcurrentHashMap.newKeySet();
        this.members.add(leader);
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID player) {
        members.add(player);
    }

    public void removeMember(UUID player) {
        members.remove(player);
    }

    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    public boolean isLeader(UUID player) {
        return leader.equals(player);
    }

    // 移除 isPublic() 和 checkPassword() 方法

    public int getSize() {
        return members.size();
    }
}
