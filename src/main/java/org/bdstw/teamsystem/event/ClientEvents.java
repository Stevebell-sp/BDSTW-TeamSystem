package org.bdstw.teamsystem.event;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.bdstw.teamsystem.TeamSystem;
import org.bdstw.teamsystem.config.TeamSystemConfig;
import org.lwjgl.glfw.GLFW;

// 註冊這個類別來監聽客戶端專屬的 FORGE 事件
@Mod.EventBusSubscriber(modid = TeamSystem.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    /**
     * 當玩家按下鍵盤按鍵時觸發此事件。
     * @param event 包含按鍵資訊的事件物件。
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // 檢查設定檔中的功能開關是否為 true
        if (!TeamSystemConfig.DISABLE_F3_FOR_NON_OPS.get()) {
            return;
        }

        // 檢查被按下的鍵是否為 F3
        if (event.getKey() == GLFW.GLFW_KEY_F3) {
            Minecraft mc = Minecraft.getInstance();

            // 檢查玩家是否存在，以及權限等級是否小於 2 (即非 OP)
            if (mc.player != null && !mc.player.hasPermissions(2)) {
                // 取消事件，這會阻止 F3 除錯畫面被開啟
                event.setCanceled(true);

                // (可選) 向玩家發送一條提示訊息，告知他們沒有權限
                mc.player.sendSystemMessage(Component.translatable("commands.bdstw_teamsystem.error.f3_disabled"));
            }
        }
    }
}
