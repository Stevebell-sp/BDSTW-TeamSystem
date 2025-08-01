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

    // 權限檢查：檢查是否在隊伍中
    private static final Predicate<CommandSourceStack> IS_IN_TEAM = source -> {
        try {
            return TeamManager.isInTeam(source.getPlayerOrException());
        } catch (CommandSyntaxException e) {
            return false;
        }
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("team")
                .requires(source -> true) // 主指令對所有人開放

                // --- 對所有玩家開放的指令 ---
                .then(Commands.literal("list")
                        .executes(TeamCommand::listTeams))

                // --- 僅限在隊伍中的玩家使用 ---
                .then(Commands.literal("info").requires(IS_IN_TEAM)
                        .executes(TeamCommand::teamInfo))
        );
    }

    private static int listTeams(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSystemMessage(Component.translatable("commands.bdstw_teamsystem.list.header", TeamManager.getAllTeams().size()));
        TeamManager.getAllTeams().forEach(team -> {
            // 處理隊長是系統的情況
            String leaderName = "系統";
            if (!team.isLeader(TeamManager.SYSTEM_UUID)) {
                ServerPlayer leader = source.getServer().getPlayerList().getPlayer(team.getLeader());
                if (leader != null) {
                    leaderName = leader.getName().getString();
                }
            }

            String privacy = team.isPublic() ? "list.public" : "list.private";
            source.sendSystemMessage(Component.translatable("commands.bdstw_teamsystem.list.entry",
                    team.getName(),
                    leaderName,
                    team.getSize(),
                    TeamSystemConfig.MAX_TEAM_SIZE.get(),
                    Component.translatable("commands.bdstw_teamsystem." + privacy)
            ));
        });
        return 1;
    }

    private static int teamInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Team team = TeamManager.getTeam(player);

        // 處理隊長是系統的情況
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

    private static void sendError(CommandContext<CommandSourceStack> context, String key, Object... args) {
        context.getSource().sendFailure(Component.translatable(PREFIX).append(Component.translatable("commands.bdstw_teamsystem." + key, args)));
    }
}
