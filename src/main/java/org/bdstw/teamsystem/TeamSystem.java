package org.bdstw.teamsystem;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.bdstw.teamsystem.config.TeamSystemConfig;
import org.bdstw.teamsystem.event.ServerEvents;
import org.bdstw.teamsystem.team.TeamManager;
import org.slf4j.Logger;

@Mod(TeamSystem.MODID)
public class TeamSystem {
    public static final String MODID = "bdstw_teamsystem";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TeamSystem() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TeamSystemConfig.SPEC, "bdstw_teamsystem-server.toml");
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("BDSTW TeamSystem Common Setup");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("========================================");
        LOGGER.info(" BDSTW TeamSystem 模組已成功載入！");
        LOGGER.info(" 浩浩大笨笨~~~！！！  by小誠");
        LOGGER.info("========================================");

        // 修正：註解掉完整的隊伍初始化
        // TeamManager.resetAndInitializeData(event.getServer());

        // 新增：僅初始化 random 指令需要的計分板
        LOGGER.info(" 正在初始化隨機指令系統...");
        TeamManager.setupRandomVersionObjective(event.getServer());
        LOGGER.info(" 隨機指令系統準備就緒！");

        LOGGER.info("========================================");
    }
}
