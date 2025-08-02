package org.bdstw.teamsystem.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TeamSystemConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

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
