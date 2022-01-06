package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.commands.NpcCommand;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class MountCommand {
    public static void registerNode(LiteralCommandNode<CommandSourceStack> editNode) {
        LiteralCommandNode<CommandSourceStack> mountNode = literal("mount")
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.mount", config.perms.npcCommandPermissionLevel))
                .then(argument("entity", EntityArgument.entity())
                        .executes(MountCommand::mountTaterzen)
                )
                .executes(MountCommand::mountTaterzen)
                .build();

        editNode.addChild(mountNode);
    }

    public static int mountTaterzen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack src = context.getSource();
        Entity toMount = null;
        try {
            toMount = EntityArgument.getEntity(context, "entity");
        } catch (IllegalArgumentException ignored) {
        }

        Entity finalToMount = toMount;
        Entity entity = src.getEntityOrException();
        return NpcCommand.selectedTaterzenExecutor(entity, taterzen -> {
            MutableComponent feedbackText;
            if(finalToMount == null) {
                taterzen.stopRiding();
                feedbackText = successText("taterzens.command.umount", taterzen.getName().getString());
            } else {
                taterzen.startRiding(finalToMount, true);
                feedbackText = successText("taterzens.command.mount", taterzen.getName().getString(), finalToMount.getName().getString());
            }
            taterzen.sendProfileUpdates();
            src.sendSuccess(feedbackText, false);
        });
    }
}
