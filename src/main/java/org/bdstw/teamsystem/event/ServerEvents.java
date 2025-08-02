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
import org.bdstw.teamsystem.command.RandomSequenceCommand; // 新增 import
import org.bdstw.teamsystem.command.TeamAdminCommand;
import org.bdstw.teamsystem.command.TeamCommand;
import org.bdstw.teamsystem.team.Team;
import org.bdstw.teamsystem.team.TeamManager;

public class ServerEvents {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TeamCommand.register(event.getDispatcher());
        TeamAdminCommand.register(event.getDispatcher());
        RandomCommand.register(event.getDispatcher());
        RandomSequenceCommand.register(event.getDispatcher()); // 新增指令註冊
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TeamManager.resetPlayerKills(player);
            Team team = TeamManager.getTeam(player);
            if (team != null) {
                TeamManager.addPlayerToScoreboardTeam(player, team);
            } else {
                TeamManager.addPlayerToLoneWolfTeam(player);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // ...
    }

    @SubscribeEvent
    public void onChatMessage(ServerChatEvent event) {
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
    }
}
