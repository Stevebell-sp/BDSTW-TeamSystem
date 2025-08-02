package org.bdstw.teamsystem.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TeamSystemConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MAX_TEAM_SIZE;

    // 新增：F3 功能開關的設定
    public static final ForgeConfigSpec.BooleanValue DISABLE_F3_FOR_NON_OPS;

    static {

        BUILDER.push("Team Settings");
        MAX_TEAM_SIZE = BUILDER
                .comment("一個隊伍的最大玩家數量")
                .defineInRange("maxTeamSize", 8, 2, 64);
        BUILDER.pop();

        // 新增：客戶端專屬設定區塊
        BUILDER.push("Client Settings");
        DISABLE_F3_FOR_NON_OPS = BUILDER
                .comment("若設為 true，將會禁用非管理員玩家的 F3 除錯畫面。")
                .define("disableF3ForNonOps", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
