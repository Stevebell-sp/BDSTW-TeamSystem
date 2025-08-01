package org.bdstw.teamsystem.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TeamSystemConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MAX_TEAM_SIZE;
    // 友軍傷害的設定已被移除，現在由程式碼直接控制

    public static final ForgeConfigSpec.IntValue RTP_MIN_RANGE;
    public static final ForgeConfigSpec.IntValue RTP_MAX_RANGE;
    public static final ForgeConfigSpec.IntValue RTP_COOLDOWN;
    public static final ForgeConfigSpec.BooleanValue RTP_IN_NETHER;
    public static final ForgeConfigSpec.BooleanValue RTP_IN_END;

    static {
        BUILDER.push("Team Settings");
        MAX_TEAM_SIZE = BUILDER
                .comment("一個隊伍的最大玩家數量")
                .defineInRange("maxTeamSize", 8, 2, 64);

        // 友軍傷害的設定已被移除

        BUILDER.pop();

        BUILDER.push("RTP Settings");
        RTP_MIN_RANGE = BUILDER
                .comment("隨機傳送的最小半徑 (以出生點為中心)")
                .defineInRange("rtpMinRange", 1000, 100, 100000);
        RTP_MAX_RANGE = BUILDER
                .comment("隨機傳送的最大半徑 (以出生點為中心)")
                .defineInRange("rtpMaxRange", 10000, 1000, 200000);
        RTP_COOLDOWN = BUILDER
                .comment("隨機傳送的冷卻時間 (秒)")
                .defineInRange("rtpCooldown", 300, 0, 3600);
        RTP_IN_NETHER = BUILDER
                .comment("是否允許在地獄使用RTP")
                .define("rtpInNether", true);
        RTP_IN_END = BUILDER
                .comment("是否允許在終界使用RTP")
                .define("rtpInEnd", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
