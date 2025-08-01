package org.bdstw.teamsystem.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bdstw.teamsystem.team.Team;
import org.bdstw.teamsystem.team.TeamManager;
import org.bdstw.teamsystem.util.RTPUtil;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TeamAdminCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("teamadmin")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("create")
                        .then(Commands.argument("team_name", StringArgumentType.string())
                                .executes(TeamAdminCommand::createTeam)))
                .then(Commands.literal("disband")
                        .then(Commands.argument("team_name", StringArgumentType.string())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        TeamManager.getAllTeams().stream().map(Team::getName)
                                                .filter(name -> !TeamManager.getPredefinedTeamNames().contains(name))
                                                .map(name -> "\"" + name + "\""), builder))
                                .executes(TeamAdminCommand::disbandTeam)))
                .then(Commands.literal("forcejoin")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("team_name", StringArgumentType.string())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                TeamManager.getAllTeams().stream().map(Team::getName).map(name -> "\"" + name + "\""), builder))
                                        .executes(TeamAdminCommand::forceJoin))))
                .then(Commands.literal("removeplayer")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(TeamAdminCommand::removePlayer)))
                .then(Commands.literal("randomjoin")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(TeamAdminCommand::forceRandomJoin)))
                .then(Commands.literal("rtp")
                        .then(Commands.literal("player")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(ctx -> executePlayerRtp(ctx, null))
                                        .then(Commands.argument("max_range", IntegerArgumentType.integer(100))
                                                .executes(ctx -> executePlayerRtp(ctx, IntegerArgumentType.getInteger(ctx, "max_range"))))))
                        .then(Commands.literal("team")
                                .then(Commands.argument("team_name", StringArgumentType.string())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                TeamManager.getAllTeams().stream().map(Team::getName).map(name -> "\"" + name + "\""), builder))
                                        .executes(ctx -> executeTeamRtp(ctx, null))
                                        .then(Commands.argument("max_range", IntegerArgumentType.integer(100))
                                                .executes(ctx -> executeTeamRtp(ctx, IntegerArgumentType.getInteger(ctx, "max_range")))))))
                // 新增：重置擊殺計數的指令
                .then(Commands.literal("resetkills")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(TeamAdminCommand::resetKills)))
        );
    }

    private static int createTeam(CommandContext<CommandSourceStack> context) {
        String teamName = StringArgumentType.getString(context, "team_name");
        if (TeamManager.getTeam(teamName) != null) {
            context.getSource().sendFailure(Component.literal("隊伍 '" + teamName + "' 已存在。"));
            return 0;
        }
        TeamManager.createTeamByAdmin(teamName, context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal("已成功創建隊伍 " + teamName), true);
        return 1;
    }

    private static int disbandTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String teamName = StringArgumentType.getString(context, "team_name");
        if (TeamManager.getTeam(teamName) == null) {
            context.getSource().sendFailure(Component.literal("隊伍 '" + teamName + "' 不存在。"));
            return 0;
        }
        if (TeamManager.getPredefinedTeamNames().contains(teamName)) {
            context.getSource().sendFailure(Component.literal("無法解散預設隊伍。"));
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
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "players");
        int successCount = 0;
        for (ServerPlayer target : targets) {
            if (TeamManager.isInTeam(target)) {
                TeamManager.forceLeaveTeam(target);
                successCount++;
            }
        }

        final int finalSuccessCount = successCount;
        if (finalSuccessCount > 0) {
            context.getSource().sendSuccess(() -> Component.literal("已成功將 " + finalSuccessCount + " 位玩家移出隊伍。"), true);
        } else {
            context.getSource().sendFailure(Component.literal("指定的玩家都不在任何隊伍中。"));
        }
        return finalSuccessCount;
    }

    private static int forceRandomJoin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "players");
        List<String> predefinedTeams = TeamManager.getPredefinedTeamNames();

        if (predefinedTeams.isEmpty()) {
            context.getSource().sendFailure(Component.literal("沒有可用的預設隊伍。"));
            return 0;
        }

        Random random = new Random();
        for (ServerPlayer target : targets) {
            String randomTeamName = predefinedTeams.get(random.nextInt(predefinedTeams.size()));
            Team team = TeamManager.getTeam(randomTeamName);

            if (team != null) {
                TeamManager.forceJoinTeam(target, team);
            } else {
                context.getSource().sendFailure(Component.literal("錯誤：隨機選擇的隊伍 '" + randomTeamName + "' 不存在。"));
            }
        }

        context.getSource().sendSuccess(() -> Component.literal("已成功將 " + targets.size() + " 位玩家隨機分配至隊伍。"), true);
        return targets.size();
    }

    private static int executePlayerRtp(CommandContext<CommandSourceStack> context, @Nullable Integer maxRange) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        CommandSourceStack source = context.getSource();

        for (ServerPlayer player : players) {
            RTPUtil.teleportPlayer(player, maxRange).thenAccept(success -> {
                if (success) {
                    BlockPos pos = player.blockPosition();
                    source.sendSuccess(() -> Component.translatable("commands.bdstw_teamsystem.admin.rtp.player.success", player.getName().getString(), pos.getX(), pos.getZ()), false);
                } else {
                    source.sendFailure(Component.translatable("commands.bdstw_teamsystem.rtp.fail"));
                }
            });
        }
        return players.size();
    }

    private static int executeTeamRtp(CommandContext<CommandSourceStack> context, @Nullable Integer maxRange) throws CommandSyntaxException {
        String teamName = StringArgumentType.getString(context, "team_name");
        Team team = TeamManager.getTeam(teamName);
        CommandSourceStack source = context.getSource();

        if (team == null) {
            source.sendFailure(Component.translatable("commands.bdstw_teamsystem.error.team_not_found", teamName));
            return 0;
        }

        if (team.getMembers().isEmpty() || (team.getMembers().size() == 1 && team.getMembers().contains(TeamManager.SYSTEM_UUID))) {
            source.sendFailure(Component.translatable("commands.bdstw_teamsystem.admin.rtp.team_empty", teamName));
            return 0;
        }

        ServerLevel level = source.getLevel();

        RTPUtil.findSafeLocation(level, maxRange).thenAccept(safeLocationOpt -> {
            safeLocationOpt.ifPresentOrElse(pos -> {
                team.getMembers().stream()
                        .map(uuid -> source.getServer().getPlayerList().getPlayer(uuid))
                        .filter(java.util.Objects::nonNull)
                        .forEach(member -> member.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, member.getYRot(), member.getXRot()));
                source.sendSuccess(() -> Component.translatable("commands.bdstw_teamsystem.admin.rtp.team.success", team.getName(), pos.getX(), pos.getZ()), true);
            }, () -> {
                source.sendFailure(Component.translatable("commands.bdstw_teamsystem.rtp.fail"));
            });
        });

        return 1;
    }

    // 新增：重置擊殺計數的執行方法
    private static int resetKills(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "players");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        if (server == null) {
            return 0;
        }

        TeamManager.resetPlayersKills(server, targets);

        source.sendSuccess(() -> Component.translatable("commands.bdstw_teamsystem.admin.resetkills.success", targets.size()), true);
        return targets.size();
    }
}
