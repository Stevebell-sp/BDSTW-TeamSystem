package org.bdstw.teamsystem.team;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.bdstw.teamsystem.config.TeamSystemConfig;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TeamManager {

    private static final Map<String, Team> teams = new ConcurrentHashMap<>();
    private static final Map<UUID, String> playerTeamMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<String>> pendingInvites = new ConcurrentHashMap<>();
    private static final List<String> predefinedTeamNames = Arrays.asList("藍隊", "紅隊", "綠隊", "白隊");
    public static final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static void createPredefinedTeams() {
        for (String name : predefinedTeamNames) {
            if (getTeam(name) == null) {
                Team team = new Team(name, SYSTEM_UUID, null);
                teams.put(name, team);
            }
        }
    }

    public static List<String> getPredefinedTeamNames() {
        return predefinedTeamNames;
    }

    // 由玩家創建隊伍 (已不再使用，但保留以防未來需要)
    public static boolean createTeam(ServerPlayer leader, String teamName, @Nullable String password) {
        if (isInTeam(leader)) return false;
        if (teams.containsKey(teamName)) return false;

        Team team = new Team(teamName, leader.getUUID(), password);
        // 修正：將 'name' 改為 'teamName'
        teams.put(teamName, team);
        playerTeamMap.put(leader.getUUID(), teamName);
        return true;
    }

    // 由管理員創建隊伍
    public static void createTeamByAdmin(String teamName, @Nullable String password) {
        if (teams.containsKey(teamName)) return;
        Team team = new Team(teamName, SYSTEM_UUID, password);
        teams.put(teamName, team);
    }

    public static void forceJoinTeam(ServerPlayer player, Team team) {
        if (isInTeam(player)) {
            forceLeaveTeam(player);
        }

        if (team.getSize() >= TeamSystemConfig.MAX_TEAM_SIZE.get() && !team.getMembers().contains(SYSTEM_UUID)) {
            player.sendSystemMessage(Component.literal("無法加入，隊伍 " + team.getName() + " 已滿。"));
            return;
        }

        if (team.isLeader(SYSTEM_UUID)) {
            team.setLeader(player.getUUID());
            team.removeMember(SYSTEM_UUID);
        }

        team.addMember(player.getUUID());
        playerTeamMap.put(player.getUUID(), team.getName());

        player.sendSystemMessage(Component.literal("你已被管理員加入隊伍 " + team.getName()));
        team.getMembers().stream()
                .map(uuid -> player.getServer().getPlayerList().getPlayer(uuid))
                .filter(Objects::nonNull)
                .forEach(member -> member.sendSystemMessage(Component.translatable("commands.bdstw_teamsystem.prefix").append(Component.translatable("commands.bdstw_teamsystem.accept.notify", player.getName().getString()))));
    }

    // 由管理員解散隊伍
    public static void disbandTeamByName(String teamName) {
        Team team = getTeam(teamName);
        if (team == null) return;

        for (UUID memberId : team.getMembers()) {
            playerTeamMap.remove(memberId);
        }
        teams.remove(teamName);
    }

    // 由玩家離開隊伍 (已不再使用)
    public static void leaveTeam(ServerPlayer player) {
        // ... (這部分程式碼不再被呼叫)
    }

    // 由管理員強制玩家離開隊伍
    public static void forceLeaveTeam(ServerPlayer player) {
        Team team = getTeam(player);
        if (team == null) return;

        team.removeMember(player.getUUID());
        playerTeamMap.remove(player.getUUID());
        player.sendSystemMessage(Component.literal("你已被管理員移出隊伍 " + team.getName()));

        if (predefinedTeamNames.contains(team.getName()) && team.getSize() == 0) {
            team.addMember(SYSTEM_UUID);
            team.setLeader(SYSTEM_UUID);
            return;
        }

        if (team.getSize() > 0 && team.isLeader(player.getUUID())) {
            UUID newLeaderId = team.getMembers().stream().findFirst().orElse(SYSTEM_UUID);
            team.setLeader(newLeaderId);
        } else if (team.getSize() == 0) {
            disbandTeamByName(team.getName());
        }
    }

    @Nullable
    public static Team getTeam(String name) {
        return teams.get(name);
    }

    @Nullable
    public static Team getTeam(ServerPlayer player) {
        String teamName = playerTeamMap.get(player.getUUID());
        return teamName != null ? teams.get(teamName) : null;
    }

    public static boolean isInTeam(ServerPlayer player) {
        return playerTeamMap.containsKey(player.getUUID());
    }

    public static Collection<Team> getAllTeams() {
        return teams.values();
    }
}
