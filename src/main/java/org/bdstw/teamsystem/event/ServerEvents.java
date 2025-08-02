package org.bdstw.teamsystem.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.bdstw.teamsystem.command.RandomCommand;
import org.bdstw.teamsystem.command.RandomSequenceCommand;
import org.bdstw.teamsystem.command.TeamAdminCommand;
import org.bdstw.teamsystem.command.TeamCommand;
import org.bdstw.teamsystem.team.Team;
import org.bdstw.teamsystem.team.TeamManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerEvents {

    private final Map<UUID, String> playerTeamTracker = new HashMap<>();

    private static final String LONE_WOLF_GROUP_LEAVE_COMMAND = "groups leave (孤狼)";
    private static final Map<String, String> groupJoinCommands = Map.of(
            "藍隊", "groups join a100b1ac-cd23-4b75-a7f4-6b625c0215a1 2222",
            "紅隊", "groups join 9dcf5767-0475-40a7-b001-f1f8052bf22 1111",
            "白隊", "groups join 7f2a76bd-0bc4-4cde-9f71-5de12e24bc60 3333",
            "綠隊", "groups join 60922881-5ced-408b-8876-07e8ac5e2b40 4444"
    );

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        // 註解掉模組自身的隊伍指令
        // TeamCommand.register(event.getDispatcher());
        // TeamAdminCommand.register(event.getDispatcher());

        // 保留隨機指令
        RandomCommand.register(event.getDispatcher());
        RandomSequenceCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 註解掉擊殺計數重置功能
            // TeamManager.resetPlayerKills(player);

            // 保留原版隊伍偵測的追蹤器初始化
            var team = player.getTeam();
            String teamName = team != null ? team.getName() : "";
            playerTeamTracker.put(player.getUUID(), teamName);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            playerTeamTracker.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = event.getServer();
        if (server == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerUUID = player.getUUID();
            var currentTeam = player.getTeam();
            String currentTeamName = currentTeam != null ? currentTeam.getName() : "";
            String lastKnownTeamName = playerTeamTracker.getOrDefault(playerUUID, "");

            if (!currentTeamName.equals(lastKnownTeamName)) {
                handleTeamChange(player, lastKnownTeamName, currentTeamName);
                playerTeamTracker.put(playerUUID, currentTeamName);
            }
        }
    }

    private void handleTeamChange(ServerPlayer player, String oldTeamName, String newTeamName) {
        String joinCommand = groupJoinCommands.get(newTeamName);

        if (joinCommand != null) {
            executeCommandAsPlayer(player, LONE_WOLF_GROUP_LEAVE_COMMAND);
            executeCommandAsPlayer(player, joinCommand);
        } else if (groupJoinCommands.containsKey(oldTeamName)) {
            executeCommandAsPlayer(player, LONE_WOLF_GROUP_LEAVE_COMMAND);
        }
    }

    @SubscribeEvent
    public void onChatMessage(ServerChatEvent event) {
        /* 註解掉聊天格式化功能，恢復為原版聊天
        ServerPlayer player = event.getPlayer();
        Team team = TeamManager.getTeam(player);
        String messageContent = event.getRawText();

        MutableComponent finalPlayerDisplay;

        if (team != null) {
            MutableComponent prefix = Component.literal("[" + team.getName() + "]").withStyle(team.getColor());
            MutableComponent playerName = Component.literal(player.getName().getString()).withStyle(ChatFormatting.WHITE);
            finalPlayerDisplay = prefix.append(playerName);
        } else {
            MutableComponent prefix = Component.literal("[孤狼]").withStyle(ChatFormatting.GRAY);
            MutableComponent playerName = Component.literal(player.getName().getString()).withStyle(ChatFormatting.WHITE);
            finalPlayerDisplay = prefix.append(playerName);
        }

        Component finalMessage = finalPlayerDisplay
                .append(Component.literal(" : ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(messageContent).withStyle(ChatFormatting.WHITE));

        event.setCanceled(true);
        player.getServer().getPlayerList().broadcastSystemMessage(finalMessage, false);
        */
    }

    private void executeCommandAsPlayer(ServerPlayer player, String command) {
        if (player.getServer() != null) {
            player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), command);
        }
    }
}
