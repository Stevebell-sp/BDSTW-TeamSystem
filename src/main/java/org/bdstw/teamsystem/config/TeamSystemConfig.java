package org.bdstw.teamsystem.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TeamSystemConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // 修正：恢復 MAX_TEAM_SIZE 的定義以解決編譯錯誤
    // 由於相關功能已被停用，此設定在遊戲中不會生效
    public static final ForgeConfigSpec.IntValue MAX_TEAM_SIZE;

    static {

        BUILDER.push("Team Settings");
        MAX_TEAM_SIZE = BUILDER
                .comment("一個隊伍的最大玩家數量")
                .defineInRange("maxTeamSize", 8, 2, 64);
        BUILDER.pop();


        SPEC = BUILDER.build();
    }
}
