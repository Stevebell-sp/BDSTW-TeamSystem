package org.bdstw.teamsystem.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam; // 新增：明確導入原版 PlayerTeam 類別
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.bdstw.teamsystem.command.RandomCommand;
import org.bdstw.teamsystem.command.RandomSequenceCommand;
import org.bdstw.teamsystem.command.TeamAdminCommand;
import org.bdstw.teamsystem.command.TeamCommand;
import org.bdstw.teamsystem.team.TeamManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerEvents {

    private final Map<UUID, String> playerTeamTracker = new HashMap<>();

    private static final String LONE_WOLF_GROUP_LEAVE_COMMAND = "groups leave (孤狼)";

    private static final Map<String, String> groupJoinCommands = Map.of(
            "blue", "groups join a100b1ac-cd23-4b75-a7f4-6b625c0215a1 2222",
            "red", "groups join 764e1285-9cd1-461a-a8b9-b4c44d7ffcc5",
            "white", "groups join 7f2a76bd-0bc4-4cde-9f71-5de12e24bc60 3333",
            "green", "groups join 60922881-5ced-408b-8876-07e8ac5e2b40 4444"
    );

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TeamCommand.register(event.getDispatcher());
        TeamAdminCommand.register(event.getDispatcher());
        RandomCommand.register(event.getDispatcher());
        RandomSequenceCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TeamManager.resetPlayerKills(player);

            // 修正：改用更明確的方式從計分板取得隊伍
            PlayerTeam vanillaTeam = player.getScoreboard().getPlayersTeam(player.getScoreboardName());
            String teamName = vanillaTeam != null ? vanillaTeam.getName() : "";
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
            // 修正：改用更明確的方式從計分板取得隊伍
            PlayerTeam currentVanillaTeam = player.getScoreboard().getPlayersTeam(player.getScoreboardName());
            String currentTeamName = currentVanillaTeam != null ? currentVanillaTeam.getName() : "";
            String lastKnownTeamName = playerTeamTracker.getOrDefault(playerUUID, "");

            if (!currentTeamName.equals(lastKnownTeamName)) {
                handleTeamChange(player, lastKnownTeamName, currentTeamName);
                playerTeamTracker.put(playerUUID, currentTeamName);
            }
        }
    }

    private void handleTeamChange(ServerPlayer player, String oldTeamName, String newTeamName) {
        //player.sendSystemMessage(Component.literal("§e[除錯] §7偵測到隊伍變更: " + (oldTeamName.isEmpty() ? "無" : oldTeamName) + " -> " + (newTeamName.isEmpty() ? "無" : newTeamName)));

        String joinCommand = groupJoinCommands.get(newTeamName);

        if (joinCommand != null) {
            executeCommandForPlayer(player, LONE_WOLF_GROUP_LEAVE_COMMAND);
            executeCommandForPlayer(player, joinCommand);
        } else if (groupJoinCommands.containsKey(oldTeamName)) {
            executeCommandForPlayer(player, LONE_WOLF_GROUP_LEAVE_COMMAND);
        }
    }

    @SubscribeEvent
    public void onChatMessage(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        // 修正：改用更明確的方式從計分板取得隊伍
        PlayerTeam vanillaPlayerTeam = player.getScoreboard().getPlayersTeam(player.getScoreboardName());
        String messageContent = event.getRawText();

        MutableComponent finalPlayerDisplay;

        if (vanillaPlayerTeam != null) {
            // 修正：移除 .withStyle(vanillaPlayerTeam.getColor()) 來取消顏色
            MutableComponent prefix = Component.literal("[").append(vanillaPlayerTeam.getDisplayName()).append("]");
            MutableComponent playerName = Component.literal(player.getName().getString());
            finalPlayerDisplay = prefix.append(" ").append(playerName);
        } else {
            MutableComponent prefix = Component.literal("[孤狼]");
            MutableComponent playerName = Component.literal(player.getName().getString());
            finalPlayerDisplay = prefix.append(" ").append(playerName);
        }

        // 組合訊息，讓 Minecraft 使用預設顏色
        Component finalMessage = finalPlayerDisplay
                .append(Component.literal(" : "))
                .append(Component.literal(messageContent));

        event.setCanceled(true);
        player.getServer().getPlayerList().broadcastSystemMessage(finalMessage, false);
    }

    private void executeCommandForPlayer(ServerPlayer player, String commandPrefix) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            String fullCommand = commandPrefix + " " + player.getName().getString();
            // 修正：更新除錯訊息以反映新的執行方式
            //player.sendSystemMessage(Component.literal("§e[除錯] §7正在模擬玩家執行: /" + fullCommand));
            // 修正：使用 player.createCommandSourceStack() 來模擬玩家執行指令
            server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), fullCommand);
        }
    }
}
