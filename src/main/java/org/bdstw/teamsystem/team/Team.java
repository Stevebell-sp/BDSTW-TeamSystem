package org.bdstw.teamsystem.team;

import net.minecraft.ChatFormatting;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Team {
    private final String name;
    private UUID leader;
    private final Set<UUID> members;
    @Nullable
    private final String password;
    private final ChatFormatting color;

    public Team(String name, UUID leader, @Nullable String password, ChatFormatting color) {
        this.name = name;
        this.leader = leader;
        this.members = ConcurrentHashMap.newKeySet();
        this.members.add(leader);
        this.password = password;
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

    public boolean isPublic() {
        return password == null || password.isEmpty();
    }

    public boolean checkPassword(@Nullable String input) {
        if (isPublic()) {
            return true;
        }
        return password.equals(input);
    }

    public int getSize() {
        return members.size();
    }
}
