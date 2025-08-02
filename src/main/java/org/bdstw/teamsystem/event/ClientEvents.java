package org.bdstw.teamsystem.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.bdstw.teamsystem.TeamSystem;
import org.bdstw.teamsystem.config.TeamSystemConfig;
import org.bdstw.teamsystem.network.PacketF3BUsage;
import org.bdstw.teamsystem.network.PacketHandler;

// 註冊這個類別來監聽客戶端專屬的 FORGE 事件
@Mod.EventBusSubscriber(modid = TeamSystem.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    private static long lastPacketSentTime = 0;
    private static final long PACKET_COOLDOWN = 1000;


    /**
     * 在遊戲準備渲染任何 GUI 畫面（如 F3 除錯畫面）之前觸發。
     * @param event 包含畫面資訊的事件物件。
     */
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (!TeamSystemConfig.DISABLE_F3_FOR_NON_OPS.get()) {
            return;
        }

        if (event.getOverlay().id().equals(VanillaGuiOverlay.DEBUG_TEXT.id())) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && !mc.player.hasPermissions(2)) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * 在每個客戶端心跳週期 (Tick) 的結尾觸發。
     * 我們利用這個事件來持續檢查玩家的按鍵輸入。
     * @param event 包含 Tick 資訊的事件物件。
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !TeamSystemConfig.DISABLE_F3_FOR_NON_OPS.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && !mc.player.hasPermissions(2)) {
            long windowHandle = mc.getWindow().getWindow();

            // 檢查 F3 和 B 鍵是否「正被按住」
            boolean isF3Down = InputConstants.isKeyDown(windowHandle, InputConstants.KEY_F3);
            boolean isBDown = InputConstants.isKeyDown(windowHandle, InputConstants.KEY_B);

            // 強化：只要 F3+B 被按住，就持續觸發檢查
            if (isF3Down && isBDown) {
                // 強制關閉實體碰撞箱，以防萬一
                if (mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
                    mc.getEntityRenderDispatcher().setRenderHitBoxes(false);
                }

                long currentTime = System.currentTimeMillis();
                // 只要按住超過冷卻時間，就發送一次封包
                if (currentTime - lastPacketSentTime > PACKET_COOLDOWN) {
                    PacketHandler.INSTANCE.sendToServer(new PacketF3BUsage());
                    lastPacketSentTime = currentTime; // 重置計時器
                }
            }

            // 雙重保險：如果碰撞箱因任何原因被開啟，也強制關閉它
            if (mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
                mc.getEntityRenderDispatcher().setRenderHitBoxes(false);
            }
        }
    }
}
