package org.bdstw.teamsystem.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.bdstw.teamsystem.command.RTPCommand;
import org.bdstw.teamsystem.command.TeamAdminCommand;
import org.bdstw.teamsystem.command.TeamCommand;
import org.bdstw.teamsystem.team.TeamManager;

public class ServerEvents {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TeamCommand.register(event.getDispatcher());
        RTPCommand.register(event.getDispatcher());
        // 註冊新的管理員指令
        TeamAdminCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (TeamManager.isInTeam(player)) {
                TeamManager.leaveTeam(player);
            }
        }
    }
}
