package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.commands.NpcCommand;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class RespawnPointCommand {
    public static void registerNode(LiteralCommandNode<CommandSourceStack> editNode) {
        LiteralCommandNode<CommandSourceStack> respawnNode = literal("respawn")
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.respawn", config.perms.npcCommandPermissionLevel))
                .then(literal("setCoordinates")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.respawn.coordinates", config.perms.npcCommandPermissionLevel))
                        .then(argument("coordinates", BlockPosArgument.blockPos())
                                .executes(RespawnPointCommand::setRespawnCoords)
                        )
                )
                .then(literal("toggle")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.respawn.toggle", config.perms.npcCommandPermissionLevel))
                        .then(argument("do respawn", BoolArgumentType.bool())
                                .executes(RespawnPointCommand::toggleRespawn)
                        )
                )
                .build();

        editNode.addChild(respawnNode);
    }

    private static int toggleRespawn(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack src = context.getSource();
        Vec3 respawnPos = BoolArgumentType.getBool(context, "do respawn") ? context.getSource().getPosition() : null;
        return NpcCommand.selectedTaterzenExecutor(src.getEntityOrException(), taterzen -> {
            taterzen.setRespawnPos(respawnPos);
            src.sendSuccess(successText("taterzens.command.respawn.toggle", String.valueOf(respawnPos == null)), false);
        });

    }

    private static int setRespawnCoords(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack src = context.getSource();
        BlockPos respawnPos = BlockPosArgument.getSpawnablePos(context, "coordinates");
        return NpcCommand.selectedTaterzenExecutor(src.getEntityOrException(), taterzen -> {
            taterzen.setRespawnPos(Vec3.atCenterOf(respawnPos));
            src.sendSuccess(successText("taterzens.command.respawn.coordinates", respawnPos.toShortString()), false);
        });
    }
}
