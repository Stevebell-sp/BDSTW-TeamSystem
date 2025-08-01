package org.bdstw.teamsystem.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.bdstw.teamsystem.team.Team;
import org.bdstw.teamsystem.team.TeamManager;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class TeamAdminCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("teamadmin")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("create")
                        .then(Commands.argument("team_name", StringArgumentType.greedyString())
                                .executes(ctx -> createTeam(ctx, null))
                                .then(Commands.argument("password", StringArgumentType.word())
                                        .executes(ctx -> createTeam(ctx, StringArgumentType.getString(ctx, "password"))))))
                .then(Commands.literal("disband")
                        .then(Commands.argument("team_name", StringArgumentType.greedyString())
                                .executes(TeamAdminCommand::disbandTeam)))
                .then(Commands.literal("forcejoin")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("team_name", StringArgumentType.greedyString())
                                        .executes(TeamAdminCommand::forceJoin))))
                .then(Commands.literal("removeplayer")
                        .then(Commands.argument("player", EntityArgument.player()) // 註：此處仍為單一玩家
                                .executes(TeamAdminCommand::removePlayer)))
                .then(Commands.literal("randomjoin")
                        // 修正：將參數從 player 改為 players，並使用 EntityArgument.players()
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(TeamAdminCommand::forceRandomJoin)))
        );
    }

    private static int createTeam(CommandContext<CommandSourceStack> context, String password) {
        String teamName = StringArgumentType.getString(context, "team_name");
        if (TeamManager.getTeam(teamName) != null) {
            context.getSource().sendFailure(Component.literal("隊伍 '" + teamName + "' 已存在。"));
            return 0;
        }
        TeamManager.createTeamByAdmin(teamName, password, context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal("已成功創建隊伍 " + teamName), true);
        return 1;
    }

    private static int disbandTeam(CommandContext<CommandSourceStack> context) {
        String teamName = StringArgumentType.getString(context, "team_name");
        if (TeamManager.getTeam(teamName) == null) {
            context.getSource().sendFailure(Component.literal("隊伍 '" + teamName + "' 不存在。"));
            return 0;
        }
        TeamManager.disbandTeamByName(teamName, context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal("已成功解散隊伍 " + teamName), true);
        return 1;
    }

    private static int forceJoin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        String teamName = StringArgumentType.getString(context, "team_name");
        Team team = TeamManager.getTeam(teamName);

        if (team == null) {
            context.getSource().sendFailure(Component.literal("隊伍 '" + teamName + "' 不存在。"));
            return 0;
        }

        TeamManager.forceJoinTeam(target, team);
        context.getSource().sendSuccess(() -> Component.literal("已強制將 " + target.getName().getString() + " 加入隊伍 " + team.getName()), true);
        return 1;
    }

    private static int removePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        if (!TeamManager.isInTeam(target)) {
            context.getSource().sendFailure(Component.literal(target.getName().getString() + " 不在任何隊伍中。"));
            return 0;
        }
        TeamManager.forceLeaveTeam(target);
        context.getSource().sendSuccess(() -> Component.literal("已強制將 " + target.getName().getString() + " 移出隊伍。"), true);
        return 1;
    }

    // 修正：更新方法以處理多個玩家
    private static int forceRandomJoin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        // 從指令參數中取得多個玩家
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "players");
        List<String> predefinedTeams = TeamManager.getPredefinedTeamNames();

        if (predefinedTeams.isEmpty()) {
            context.getSource().sendFailure(Component.literal("沒有可用的預設隊伍。"));
            return 0;
        }

        Random random = new Random();
        for (ServerPlayer target : targets) {
            // 為每個玩家隨機選擇一個隊伍
            String randomTeamName = predefinedTeams.get(random.nextInt(predefinedTeams.size()));
            Team team = TeamManager.getTeam(randomTeamName);

            if (team != null) {
                TeamManager.forceJoinTeam(target, team);
            } else {
                // 這種情況理論上不應發生
                context.getSource().sendFailure(Component.literal("錯誤：隨機選擇的隊伍 '" + randomTeamName + "' 不存在。"));
            }
        }

        context.getSource().sendSuccess(() -> Component.literal("已成功將 " + targets.size() + " 位玩家隨機分配至隊伍。"), true);
        return targets.size();
    }
}
