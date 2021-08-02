package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import org.samo_lego.taterzens.commands.NpcCommand;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class MountCommand {
    public static void registerNode(LiteralCommandNode<ServerCommandSource> editNode) {
        LiteralCommandNode<ServerCommandSource> mountNode = literal("mount")
                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.mount", config.perms.npcCommandPermissionLevel))
                .then(argument("entity", EntityArgumentType.entity())
                        .executes(MountCommand::mountTaterzen)
                )
                .executes(MountCommand::mountTaterzen)
                .build();

        editNode.addChild(mountNode);
    }

    public static int mountTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource src = context.getSource();
        Entity toMount = null;
        try {
            toMount = EntityArgumentType.getEntity(context, "entity");
        } catch (IllegalArgumentException ignored) {
        }

        Entity finalToMount = toMount;
        Entity entity = src.getEntityOrThrow();
        return NpcCommand.selectedTaterzenExecutor(entity, taterzen -> {
            MutableText feedbackText;
            if(finalToMount == null) {
                taterzen.stopRiding();
                feedbackText = successText("taterzens.command.umount", taterzen.getName().getString());
            } else {
                taterzen.startRiding(finalToMount, true);
                feedbackText = successText("taterzens.command.mount", taterzen.getName().getString(), finalToMount.getName().getString());
            }
            taterzen.sendProfileUpdates();
            src.sendFeedback(feedbackText, false);
        });
    }
}
