package org.bdstw.teamsystem.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

public class RandomCommand {

    private static final SimpleCommandExceptionType ERROR_INVALID_RANGE = new SimpleCommandExceptionType(
            Component.translatable("commands.bdstw_teamsystem.random.error.invalid_range"));
    private static final Dynamic2CommandExceptionType ERROR_RANGE_TOO_LARGE = new Dynamic2CommandExceptionType(
            (max, min) -> Component.translatable("commands.bdstw_teamsystem.random.error.range_too_large", max, min));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("random")
                        .requires(source -> source.hasPermission(2))
                        // /random value <range> [sequence]
                        .then(Commands.literal("value")
                                .then(Commands.argument("range", RangeArgument.intRange())
                                        .executes(ctx -> executeRandom(ctx, false, null))
                                        .then(Commands.argument("sequence", ResourceLocationArgument.id())
                                                .executes(ctx -> executeRandom(ctx, false, ResourceLocationArgument.getId(ctx, "sequence"))))))
                        // /random roll <range> [sequence]
                        .then(Commands.literal("roll")
                                .then(Commands.argument("range", RangeArgument.intRange())
                                        .executes(ctx -> executeRandom(ctx, true, null))
                                        .then(Commands.argument("sequence", ResourceLocationArgument.id())
                                                .executes(ctx -> executeRandom(ctx, true, ResourceLocationArgument.getId(ctx, "sequence"))))))
                // 注意：由於 Minecraft 1.20.1 API 的限制，reset 功能已被移除。
        );
    }

    private static int executeRandom(CommandContext<CommandSourceStack> context, boolean isRoll, ResourceLocation sequenceId) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        // 修正：這是 1.20.1 中獲取 RangeArgument 的正確方式
        MinMaxBounds.Ints range = context.getArgument("range", MinMaxBounds.Ints.class);
        int min = range.getMin() != null ? range.getMin() : Integer.MIN_VALUE;
        int max = range.getMax() != null ? range.getMax() : Integer.MAX_VALUE;

        if (min > max) {
            throw ERROR_INVALID_RANGE.create();
        }

        // 使用 long 來計算以避免在比較前發生溢位
        long rangeSize = (long) max - (long) min;
        if (rangeSize >= Integer.MAX_VALUE) {
            throw ERROR_RANGE_TOO_LARGE.create(max, min);
        }

        RandomSource randomSource = (sequenceId != null)
                ? level.getRandomSequences().get(sequenceId)
                : source.getLevel().getRandom();

        int result = randomSource.nextIntBetweenInclusive(min, max);

        if (isRoll) {
            // 公開擲骰
            source.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("commands.bdstw_teamsystem.random.roll.broadcast",
                            source.getDisplayName(), result, min, max), false);
        } else {
            // 私下取值
            source.sendSuccess(() -> Component.translatable("commands.bdstw_teamsystem.random.value.success", result), false);
        }

        return result;
    }
}
