package org.bdstw.teamsystem.network;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class PacketF3BUsage {

    // 用於在伺服器端追蹤玩家的違規次數
    private static final Map<UUID, Integer> violationCounter = new HashMap<>();

    public PacketF3BUsage() {
        // 這個封包不需要傳遞任何資料
    }

    public static void encode(PacketF3BUsage msg, FriendlyByteBuf buf) {
        // 空白
    }

    public static PacketF3BUsage decode(FriendlyByteBuf buf) {
        return new PacketF3BUsage();
    }

    // 伺服器收到封包後的處理邏輯
    public static void handle(PacketF3BUsage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            UUID playerUUID = player.getUUID();
            int violations = violationCounter.getOrDefault(playerUUID, 0) + 1;
            violationCounter.put(playerUUID, violations);

            // 公告伺服器
            player.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("commands.bdstw_teamsystem.anticheat.f3b_warning", player.getName().getString()), false);

            // 如果違規次數達到 3 次
            if (violations >= 3) {
                // 重置計數器
                violationCounter.put(playerUUID, 0);

                // 第一次擊殺
                player.kill();
                player.getServer().getPlayerList().broadcastSystemMessage(
                        Component.translatable("commands.bdstw_teamsystem.anticheat.f3b_death", player.getName().getString()).withStyle(ChatFormatting.RED), false);

                // 安排 2 秒後的第二次擊殺
                CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS).execute(() -> {
                    // 再次檢查玩家是否還在線上
                    if (player.isAlive()) {
                        player.kill();
                    }

                    // 新增：在第二次擊殺後，執行計分板指令並公告
                    String command = "scoreboard players set " + player.getName().getString() + " cheaters 1";
                    player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack(), command);
                    player.getServer().getPlayerList().broadcastSystemMessage(
                            Component.translatable("commands.bdstw_teamsystem.anticheat.f3b_marked", player.getName().getString()).withStyle(ChatFormatting.DARK_RED), false);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
