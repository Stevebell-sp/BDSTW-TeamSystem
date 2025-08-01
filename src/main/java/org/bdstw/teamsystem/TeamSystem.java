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
        LOGGER.info(" 正在建立預設隊伍...");
        // 修正：不再需要傳遞任何參數
        TeamManager.createPredefinedTeams();
        LOGGER.info(" 預設隊伍建立完成！");
        LOGGER.info("========================================");
    }
}
