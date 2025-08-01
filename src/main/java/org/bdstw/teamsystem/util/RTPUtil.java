package org.bdstw.teamsystem.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bdstw.teamsystem.config.TeamSystemConfig;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class RTPUtil {

    private static final Random RANDOM = new Random();
    private static final int MAX_TRIES = 150; // 大幅增加嘗試次數

    public static CompletableFuture<Optional<BlockPos>> findSafeLocation(ServerLevel level, @Nullable Integer customMaxRange) {
        if (isDimensionBlacklisted(level)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        WorldBorder border = level.getWorldBorder();
        int minRange = TeamSystemConfig.RTP_MIN_RANGE.get();
        int maxRange = (customMaxRange != null && customMaxRange > minRange) ? customMaxRange : TeamSystemConfig.RTP_MAX_RANGE.get();

        int spawnX = level.getSharedSpawnPos().getX();
        int spawnZ = level.getSharedSpawnPos().getZ();

        for (int i = 0; i < MAX_TRIES; i++) {
            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            double distance = minRange + (RANDOM.nextDouble() * (maxRange - minRange));

            int x = spawnX + (int) (distance * Math.cos(angle));
            int z = spawnZ + (int) (distance * Math.sin(angle));

            if (!isWithinBorder(border, x, z)) {
                continue; // 如果超出邊界，就再試一次
            }

            // 使用更可靠的高度圖
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y > level.getMinBuildHeight()) {
                BlockPos potentialPos = new BlockPos(x, y, z);
                if (isSafe(level, potentialPos)) {
                    // 確保區塊已載入
                    return level.getChunkSource().getChunkFuture(potentialPos.getX() >> 4, potentialPos.getZ() >> 4, ChunkStatus.FULL, true)
                            .thenApply(either -> Optional.of(potentialPos));
                }
            }
        }

        return CompletableFuture.completedFuture(Optional.empty());
    }

    private static boolean isSafe(ServerLevel level, BlockPos pos) {
        BlockPos groundPos = pos.below();
        if (groundPos.getY() < level.getMinBuildHeight()) {
            return false;
        }

        BlockState groundState = level.getBlockState(groundPos);

        // 腳下不能是危險方塊、非固體方塊或樹葉
        if (!groundState.blocksMotion() || groundState.is(BlockTags.LEAVES) || groundState.is(BlockTags.FIRE) || groundState.is(Blocks.MAGMA_BLOCK) || groundState.is(Blocks.CACTUS)) {
            return false;
        }

        // 傳送點不能在任何液體中
        if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.above()).isEmpty() || !groundState.getFluidState().isEmpty()) {
            return false;
        }

        // 玩家需要足夠的站立空間 (2格高)
        return level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above());
    }

    private static boolean isWithinBorder(WorldBorder border, int x, int z) {
        return x >= border.getMinX() && x <= border.getMaxX() && z >= border.getMinZ() && z <= border.getMaxZ();
    }

    private static boolean isDimensionBlacklisted(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        return (!TeamSystemConfig.RTP_IN_NETHER.get() && dimension.equals(Level.NETHER)) ||
                (!TeamSystemConfig.RTP_IN_END.get() && dimension.equals(Level.END));
    }

    public static CompletableFuture<Boolean> teleportPlayer(ServerPlayer player, @Nullable Integer maxRange) {
        ServerLevel level = player.serverLevel();
        return findSafeLocation(level, maxRange).thenApply(safeLocationOpt -> {
            safeLocationOpt.ifPresent(pos -> {
                player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
            });
            return safeLocationOpt.isPresent();
        });
    }
}
