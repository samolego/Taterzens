package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.npc.NPCData;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.errorText;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class ActionCommand {
    public static void registerNode(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> actionNode = literal("action")
                .then(literal("goto")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.action.goto", config.perms.npcCommandPermissionLevel))
                        .then(argument("block pos", BlockPosArgument.blockPos())
                                .executes(ActionCommand::gotoBlock)
                        )
                )
                .then(literal("interact")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.action.interact", config.perms.npcCommandPermissionLevel))
                        .then(argument("block pos", BlockPosArgument.blockPos())
                                .executes(ActionCommand::interactWithBlock)
                        )
                )
                .build();

        npcNode.addChild(actionNode);
    }

    private static int interactWithBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        BlockPos pos = BlockPosArgument.getSpawnablePos(context, "block pos");
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            if(taterzen.interact(pos)) {
                source.sendSuccess(
                        successText("taterzens.command.action.interact.success", taterzen.getName().getString(), pos.toShortString()),
                        false
                );
            } else {

                source.sendSuccess(
                        errorText("taterzens.command.action.interact.fail", pos.toShortString()),
                        false
                );
            }
        });
    }

    private static int gotoBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        BlockPos pos = BlockPosArgument.getSpawnablePos(context, "block pos");
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            taterzen.setMovement(NPCData.Movement.TICK);

            taterzen.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1);

            source.sendSuccess(
                    successText("taterzens.command.action.goto.success", taterzen.getName().getString(), pos.toShortString()),
                    false
            );
        });
    }
}
