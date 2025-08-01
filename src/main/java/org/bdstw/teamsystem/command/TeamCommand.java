package org.bdstw.teamsystem.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.bdstw.teamsystem.config.TeamSystemConfig;
import org.bdstw.teamsystem.team.Team;
import org.bdstw.teamsystem.team.TeamManager;

import java.util.Objects;
import java.util.function.Predicate;

public class TeamCommand {

    private static final String PREFIX = "commands.bdstw_teamsystem.prefix";

    private static final Predicate<CommandSourceStack> IS_IN_TEAM = source -> {
        try {
            return TeamManager.isInTeam(source.getPlayerOrException());
        } catch (CommandSyntaxException e) {
            return false;
        }
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("team")
                .requires(source -> true)
                .then(Commands.literal("list")
                        .executes(TeamCommand::listTeams))
                .then(Commands.literal("info").requires(IS_IN_TEAM)
                        .executes(TeamCommand::teamInfo))
        );
    }

    private static int listTeams(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSystemMessage(Component.translatable("commands.bdstw_teamsystem.list.header", TeamManager.getAllTeams().size()));
        TeamManager.getAllTeams().forEach(team -> {
            String leaderName = "系統";
            if (!team.isLeader(TeamManager.SYSTEM_UUID)) {
                ServerPlayer leader = source.getServer().getPlayerList().getPlayer(team.getLeader());
                if (leader != null) {
                    leaderName = leader.getName().getString();
                }
            }

            // 修正：移除隱私狀態的顯示
            source.sendSystemMessage(Component.translatable("commands.bdstw_teamsystem.list.entry",
                    team.getName(),
                    leaderName,
                    team.getSize(),
                    TeamSystemConfig.MAX_TEAM_SIZE.get()
            ));
        });
        return 1;
    }

    private static int teamInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Team team = TeamManager.getTeam(player);

        String leaderName = "系統";
        if (!team.isLeader(TeamManager.SYSTEM_UUID)) {
            ServerPlayer leader = context.getSource().getServer().getPlayerList().getPlayer(team.getLeader());
            if (leader != null) {
                leaderName = leader.getName().getString();
            }
        }

        context.getSource().sendSystemMessage(Component.translatable("commands.bdstw_teamsystem.info.header", team.getName()));
        context.getSource().sendSystemMessage(Component.translatable("commands.bdstw_teamsystem.info.leader", leaderName));
        context.getSource().sendSystemMessage(Component.translatable("commands.bdstw_teamsystem.info.members"));
        team.getMembers().stream()
                .map(uuid -> context.getSource().getServer().getPlayerList().getPlayer(uuid))
                .filter(Objects::nonNull)
                .forEach(p -> context.getSource().sendSystemMessage(Component.translatable("commands.bdstw_teamsystem.info.member_entry", p.getName().getString())));

        return 1;
    }
}
