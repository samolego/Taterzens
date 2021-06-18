package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.PERMISSIONS;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.commands.NpcCommand.selectedTaterzenExecutor;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;

public class TeleportCommand {

    public static void registerNode(LiteralCommandNode<ServerCommandSource> npcNode) {
        LiteralCommandNode<ServerCommandSource> tpNode = literal("tp")
                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_tp, config.perms.npcCommandPermissionLevel))
                .then(argument("destination", EntityArgumentType.entity())
                        .executes(context -> teleportTaterzen(context, EntityArgumentType.getEntity(context, "destination").getPos()))
                )
                .then(argument("position", Vec3ArgumentType.vec3())
                        .executes(context -> teleportTaterzen(context, Vec3ArgumentType.getPosArgument(context, "position").toAbsolutePos(context.getSource())))
                )
                .build();

        npcNode.addChild(tpNode);
    }

    private static int teleportTaterzen(CommandContext<ServerCommandSource> context, Vec3d destination) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        return selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            taterzen.teleport(destination.getX(), destination.getY(), destination.getZ());
        });
    }
}
