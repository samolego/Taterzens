package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.taterzens.api.TaterzensAPI;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.NPCData;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.samo_lego.taterzens.npc.ai.goal.DirectPathGoal;

import java.io.File;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.Taterzens.presetsDir;
import static org.samo_lego.taterzens.api.TaterzensAPI.getPresets;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.errorText;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class ActionCommand {
    public static void registerNode(LiteralCommandNode<ServerCommandSource> npcNode) {
        LiteralCommandNode<ServerCommandSource> actionNode = literal("action")
                .then(literal("goto")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.action.goto", config.perms.npcCommandPermissionLevel))
                        .then(argument("block pos", BlockPosArgumentType.blockPos())
                                .executes(ActionCommand::gotoBlock)
                        )
                )
                .then(literal("interact")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.action.interact", config.perms.npcCommandPermissionLevel))
                        .then(argument("block pos", BlockPosArgumentType.blockPos())
                                .executes(ActionCommand::interactWithBlock)
                        )
                )
                .build();

        npcNode.addChild(actionNode);
    }

    private static int interactWithBlock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "block pos");
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrThrow(), taterzen -> {
            if(taterzen.interact(pos)) {
                source.sendFeedback(
                        successText("taterzens.command.action.interact.success", pos.toShortString()),
                        false
                );
            } else {

                source.sendFeedback(
                        errorText("taterzens.command.action.interact.fail", pos.toShortString()),
                        false
                );
            }


        });


    }

    private static int gotoBlock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "block pos");
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrThrow(), taterzen -> {
            taterzen.setMovement(NPCData.Movement.TICK);

            taterzen.getNavigation().startMovingTo(pos.getX(), pos.getY(), pos.getZ(), 1);

            source.sendFeedback(
                    successText("taterzens.command.action.goto.success", taterzen.getName().getString(), pos.toShortString()),
                    false
            );
        });
    }
}
