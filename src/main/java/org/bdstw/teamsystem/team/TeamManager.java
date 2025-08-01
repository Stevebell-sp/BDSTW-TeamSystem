package org.bdstw.teamsystem.team;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bdstw.teamsystem.config.TeamSystemConfig;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeamManager {

    private static final Map<String, Team> teams = new ConcurrentHashMap<>();
    private static final Map<UUID, String> playerTeamMap = new ConcurrentHashMap<>();
    private static final Map<String, ChatFormatting> predefinedTeamInfo = Map.of(
            "藍隊", ChatFormatting.BLUE,
            "紅隊", ChatFormatting.RED,
            "綠隊", ChatFormatting.GREEN,
            "白隊", ChatFormatting.WHITE
    );
    private static final List<ChatFormatting> availableColors = Arrays.asList(
            ChatFormatting.GOLD, ChatFormatting.AQUA, ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.YELLOW, ChatFormatting.GRAY, ChatFormatting.DARK_PURPLE,
            ChatFormatting.DARK_AQUA, ChatFormatting.DARK_GREEN, ChatFormatting.DARK_RED
    );
    private static int nextColorIndex = 0;

    public static final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final String LONE_WOLF_TEAM_NAME = "lone_wolf_team";
    private static final String KILLS_OBJECTIVE_NAME = "playerKills";

    public static void resetAndInitializeData(MinecraftServer server) {
        cleanupOldScoreboardTeams(server);
        teams.clear();
        playerTeamMap.clear();
        nextColorIndex = 0;
        createLoneWolfTeam(server);
        createPredefinedTeams(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            addPlayerToLoneWolfTeam(player);
        }
        setupKillsObjective(server);
    }

    private static void cleanupOldScoreboardTeams(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        Set<String> knownTeamNames = new HashSet<>(predefinedTeamInfo.keySet());
        knownTeamNames.add(LONE_WOLF_TEAM_NAME);

        for (String teamName : knownTeamNames) {
            PlayerTeam scoreboardTeam = scoreboard.getPlayerTeam(teamName);
            if (scoreboardTeam != null) {
                scoreboard.removePlayerTeam(scoreboardTeam);
            }
        }
    }

    private static void createPredefinedTeams(MinecraftServer server) {
        for (Map.Entry<String, ChatFormatting> entry : predefinedTeamInfo.entrySet()) {
            String name = entry.getKey();
            ChatFormatting color = entry.getValue();
            if (getTeam(name) == null) {
                Team team = new Team(name, SYSTEM_UUID, null, color);
                teams.put(name, team);
                registerTeamInScoreboard(team, server);
            }
        }
    }

    private static void setupKillsObjective(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        // 修正：使用 Forge 提供的標準常數，這是最穩定的作法
        ObjectiveCriteria criteria = ObjectiveCriteria.byName("playerKillCount").get();
        Component displayName = Component.literal("殺敵數").withStyle(ChatFormatting.RED);

        Objective objective = scoreboard.getObjective(KILLS_OBJECTIVE_NAME);
        if (objective == null) {
            objective = scoreboard.addObjective(KILLS_OBJECTIVE_NAME, criteria, displayName, ObjectiveCriteria.RenderType.INTEGER);
        }

        // 修正：使用 DisplaySlot.LIST 常數，而不是數字 0
        scoreboard.setDisplayObjective(0, objective); // 0 是 LIST（玩家列表），1 是 SIDEBAR，2 是 BELOW_NAME
    }

    private static void createLoneWolfTeam(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        if (scoreboard.getPlayerTeam(LONE_WOLF_TEAM_NAME) == null) {
            PlayerTeam loneWolfTeam = scoreboard.addPlayerTeam(LONE_WOLF_TEAM_NAME);
            loneWolfTeam.setDisplayName(Component.literal("孤狼"));
            loneWolfTeam.setColor(ChatFormatting.WHITE);
            MutableComponent prefix = Component.literal("[孤狼] ").withStyle(ChatFormatting.GRAY);
            loneWolfTeam.setPlayerPrefix(prefix);
            // 孤狼隊伍：允許隊友傷害
            loneWolfTeam.setAllowFriendlyFire(true);
            loneWolfTeam.setSeeFriendlyInvisibles(false);
            loneWolfTeam.setNameTagVisibility(PlayerTeam.Visibility.ALWAYS);
            loneWolfTeam.setCollisionRule(PlayerTeam.CollisionRule.ALWAYS);
        }
    }

    private static void registerTeamInScoreboard(Team team, MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        String teamName = team.getName();
        if (scoreboard.getPlayerTeam(teamName) == null) {
            PlayerTeam scoreboardTeam = scoreboard.addPlayerTeam(teamName);
            scoreboardTeam.setDisplayName(Component.literal(teamName));
            scoreboardTeam.setColor(ChatFormatting.WHITE);
            MutableComponent prefix = Component.literal("[" + teamName + "]").withStyle(team.getColor());
            scoreboardTeam.setPlayerPrefix(prefix);
            // 其他所有隊伍：禁止隊友傷害
            scoreboardTeam.setAllowFriendlyFire(false);
            scoreboardTeam.setSeeFriendlyInvisibles(true);
            scoreboardTeam.setNameTagVisibility(PlayerTeam.Visibility.ALWAYS);
            scoreboardTeam.setCollisionRule(PlayerTeam.CollisionRule.ALWAYS);
        }
    }

    public static List<String> getPredefinedTeamNames() {
        return new ArrayList<>(predefinedTeamInfo.keySet());
    }

    public static void createTeamByAdmin(String teamName, @Nullable String password, MinecraftServer server) {
        if (teams.containsKey(teamName)) return;
        ChatFormatting color = availableColors.get(nextColorIndex % availableColors.size());
        nextColorIndex++;
        Team team = new Team(teamName, SYSTEM_UUID, password, color);
        teams.put(teamName, team);
        registerTeamInScoreboard(team, server);
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
        addPlayerToScoreboardTeam(player, team);

        player.sendSystemMessage(Component.literal("你已被管理員加入隊伍 " + team.getName()));
        team.getMembers().stream()
                .map(uuid -> player.getServer().getPlayerList().getPlayer(uuid))
                .filter(Objects::nonNull)
                .forEach(member -> member.sendSystemMessage(Component.translatable("commands.bdstw_teamsystem.prefix").append(Component.translatable("commands.bdstw_teamsystem.accept.notify", player.getName().getString()))));
    }

    public static void disbandTeamByName(String teamName, MinecraftServer server) {
        Team team = getTeam(teamName);
        if (team == null) return;

        Set<UUID> membersToProcess = new HashSet<>(team.getMembers());
        for (UUID memberId : membersToProcess) {
            playerTeamMap.remove(memberId);
            ServerPlayer player = server.getPlayerList().getPlayer(memberId);
            if(player != null) {
                addPlayerToLoneWolfTeam(player);
            }
        }
        teams.remove(teamName);
        unregisterTeamFromScoreboard(teamName, server);
    }

    public static void forceLeaveTeam(ServerPlayer player) {
        Team team = getTeam(player);
        if (team == null) return;

        team.removeMember(player.getUUID());
        playerTeamMap.remove(player.getUUID());
        addPlayerToLoneWolfTeam(player);
        player.sendSystemMessage(Component.literal("你已被管理員移出隊伍 " + team.getName()));

        if (predefinedTeamInfo.containsKey(team.getName()) && team.getSize() == 0) {
            team.addMember(SYSTEM_UUID);
            team.setLeader(SYSTEM_UUID);
            return;
        }

        if (team.getSize() > 0 && team.isLeader(player.getUUID())) {
            UUID newLeaderId = team.getMembers().stream().findFirst().orElse(SYSTEM_UUID);
            team.setLeader(newLeaderId);
        } else if (team.getSize() == 0) {
            disbandTeamByName(team.getName(), player.getServer());
        }
    }

    public static void addPlayerToLoneWolfTeam(ServerPlayer player) {
        if (player.getServer() == null) return;
        Scoreboard scoreboard = player.getServer().getScoreboard();
        PlayerTeam loneWolfTeam = scoreboard.getPlayerTeam(LONE_WOLF_TEAM_NAME);
        if (loneWolfTeam != null) {
            scoreboard.addPlayerToTeam(player.getScoreboardName(), loneWolfTeam);
        }
    }

    private static void unregisterTeamFromScoreboard(String teamName, MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam scoreboardTeam = scoreboard.getPlayerTeam(teamName);
        if (scoreboardTeam != null) {
            scoreboard.removePlayerTeam(scoreboardTeam);
        }
    }

    public static void addPlayerToScoreboardTeam(ServerPlayer player, Team team) {
        if (player.getServer() == null) return;
        Scoreboard scoreboard = player.getServer().getScoreboard();
        PlayerTeam scoreboardTeam = scoreboard.getPlayerTeam(team.getName());
        if (scoreboardTeam != null) {
            scoreboard.addPlayerToTeam(player.getScoreboardName(), scoreboardTeam);
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
