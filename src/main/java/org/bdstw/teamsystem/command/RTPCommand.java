package org.bdstw.teamsystem.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.bdstw.teamsystem.config.TeamSystemConfig;
import org.bdstw.teamsystem.team.Team;
import org.bdstw.teamsystem.team.TeamManager;
import org.bdstw.teamsystem.util.RTPUtil;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RTPCommand {

    private static final String PREFIX = "commands.bdstw_teamsystem.prefix";
    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 註冊 /rtp 指令
        dispatcher.register(Commands.literal("rtp")
                .requires(source -> true)
                .executes(context -> executePersonalRtp(context, null))
                .then(Commands.argument("max_range", IntegerArgumentType.integer(100))
                        .executes(context -> executePersonalRtp(context, IntegerArgumentType.getInteger(context, "max_range")))));

        // 註冊 /team rtp 指令 (在 TeamCommand 中引用)
    }

    // 處理個人 RTP
    private static int executePersonalRtp(CommandContext<CommandSourceStack> context, @Nullable Integer maxRange) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        UUID playerId = player.getUUID();

        if (checkCooldown(player, playerId)) {
            return 0;
        }

        RTPUtil.teleportPlayer(player, maxRange).thenAccept(success -> {
            if (success) {
                BlockPos pos = player.blockPosition();
                sendSuccess(context, "rtp.success", pos.getX(), pos.getZ());
                setCooldown(playerId);
            } else {
                sendError(context, "rtp.fail");
            }
        });

        return 1;
    }

    // 處理隊伍 RTP (這個方法會被 TeamCommand 呼叫)
    public static int executeTeamRtp(CommandContext<CommandSourceStack> context, @Nullable Integer maxRange) throws CommandSyntaxException {
        ServerPlayer leader = context.getSource().getPlayerOrException();
        Team team = TeamManager.getTeam(leader);

        // 檢查是否在隊伍中
        if (team == null) {
            // 如果不在隊伍中，就執行個人傳送
            return executePersonalRtp(context, maxRange);
        }

        // 檢查是否為隊長
        if (!team.isLeader(leader.getUUID())) {
            sendError(context, "error.not_leader");
            return 0;
        }

        if (checkCooldown(leader, team.getLeader())) {
            return 0;
        }

        RTPUtil.findSafeLocation(leader.serverLevel(), maxRange).thenAccept(safeLocationOpt -> {
            safeLocationOpt.ifPresentOrElse(pos -> {
                team.getMembers().stream()
                        .map(uuid -> context.getSource().getServer().getPlayerList().getPlayer(uuid))
                        .filter(java.util.Objects::nonNull)
                        .forEach(member -> {
                            member.teleportTo(leader.serverLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, member.getYRot(), member.getXRot());
                            member.sendSystemMessage(Component.translatable(PREFIX).append(Component.translatable("commands.bdstw_teamsystem.rtp.success_team", pos.getX(), pos.getZ())));
                        });
                setCooldown(team.getLeader());
            }, () -> {
                sendError(context, "rtp.fail");
            });
        });

        return 1;
    }

    private static boolean checkCooldown(ServerPlayer player, UUID id) {
        long currentTime = System.currentTimeMillis() / 1000;
        long lastUsed = cooldowns.getOrDefault(id, 0L);
        int cooldownSeconds = TeamSystemConfig.RTP_COOLDOWN.get();

        if (currentTime - lastUsed < cooldownSeconds) {
            long timeLeft = cooldownSeconds - (currentTime - lastUsed);
            player.sendSystemMessage(Component.translatable(PREFIX).append(Component.translatable("commands.bdstw_teamsystem.rtp.cooldown", timeLeft)));
            return true;
        }
        return false;
    }

    private static void setCooldown(UUID id) {
        cooldowns.put(id, System.currentTimeMillis() / 1000);
    }

    private static void sendError(CommandContext<CommandSourceStack> context, String key, Object... args) {
        context.getSource().sendFailure(Component.translatable(PREFIX).append(Component.translatable("commands.bdstw_teamsystem." + key, args)));
    }

    private static void sendSuccess(CommandContext<CommandSourceStack> context, String key, Object... args) {
        context.getSource().sendSuccess(() -> Component.translatable(PREFIX).append(Component.translatable("commands.bdstw_teamsystem." + key, args)), true);
    }
}
