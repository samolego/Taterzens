package org.samo_lego.taterzens.common.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.taterzens.common.Taterzens;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.common.Taterzens.config;
import static org.samo_lego.taterzens.common.commands.NpcCommand.selectedTaterzenExecutor;

public class TeleportCommand {

    public static void registerNode(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> tpNode = literal("tp")
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.tp", config.perms.npcCommandPermissionLevel))
                .then(argument("entity", EntityArgument.entity())
                        .executes(context -> teleportTaterzen(context, EntityArgument.getEntity(context, "entity").position()))
                )
                .then(argument("location", Vec3Argument.vec3())
                        .executes(context -> teleportTaterzen(context, Vec3Argument.getCoordinates(context, "location").getPosition(context.getSource())))
                )
                .build();

        npcNode.addChild(tpNode);
    }

    private static int teleportTaterzen(CommandContext<CommandSourceStack> context, Vec3 destination) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> taterzen.teleportToWithTicket(destination.x(), destination.y(), destination.z()));
    }
}
