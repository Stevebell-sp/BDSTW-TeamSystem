package org.bdstw.teamsystem.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.bdstw.teamsystem.team.TeamManager;

public class RandomSequenceCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("randomsequence")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reset")
                        .then(Commands.argument("sequence", ResourceLocationArgument.id())
                                .executes(RandomSequenceCommand::executeReset)))
        );
    }

    private static int executeReset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation sequenceId = ResourceLocationArgument.getId(context, "sequence");
        Scoreboard scoreboard = context.getSource().getLevel().getScoreboard();
        Objective objective = scoreboard.getObjective(TeamManager.RANDOM_VERSION_OBJECTIVE_NAME);

        if (objective == null) {
            TeamManager.setupRandomVersionObjective(context.getSource().getServer());
            objective = scoreboard.getObjective(TeamManager.RANDOM_VERSION_OBJECTIVE_NAME);
        }

        String sequenceHolderName = sequenceId.toString();
        Score score = scoreboard.getOrCreatePlayerScore(sequenceHolderName, objective);
        score.add(1); // 將版本號 +1

        context.getSource().sendSuccess(() -> Component.translatable("commands.bdstw_teamsystem.random.reset.success", sequenceId.toString()), true);
        return 1;
    }
}
