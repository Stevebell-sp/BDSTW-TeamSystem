package org.bdstw.teamsystem.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.bdstw.teamsystem.command.RandomCommand;
import org.bdstw.teamsystem.command.RandomSequenceCommand;
import org.bdstw.teamsystem.command.TeamAdminCommand;
import org.bdstw.teamsystem.command.TeamCommand;
import org.bdstw.teamsystem.team.Team;
import org.bdstw.teamsystem.team.TeamManager;

public class ServerEvents {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        // 修正：註解掉隊伍相關指令的註冊
        // TeamCommand.register(event.getDispatcher());
        // TeamAdminCommand.register(event.getDispatcher());

        // 保留隨機指令的註冊
        RandomCommand.register(event.getDispatcher());
        RandomSequenceCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        /* 修正：暫時停用所有玩家登入時的邏輯
        if (event.getEntity() instanceof ServerPlayer player) {
            TeamManager.resetPlayerKills(player);
            Team team = TeamManager.getTeam(player);
            if (team != null) {
                TeamManager.addPlayerToScoreboardTeam(player, team);
            } else {
                TeamManager.addPlayerToLoneWolfTeam(player);
            }
        }
        */
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        /* 修正：暫時停用所有玩家登出時的邏輯
        if (event.getEntity() instanceof ServerPlayer player) {
            // ...
        }
        */
    }

    @SubscribeEvent
    public void onChatMessage(ServerChatEvent event) {
        /* 修正：暫時停用聊天格式化功能
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
}
