package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.commands.NpcCommand;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.MessageArgument.message;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class NameCommand {

    public static void registerNode(LiteralCommandNode<CommandSourceStack> editNode) {
        LiteralCommandNode<CommandSourceStack> nameNode = literal("name")
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.name", config.perms.npcCommandPermissionLevel))
                .then(argument("new name", message()).executes(NameCommand::renameTaterzen))
                .build();

        editNode.addChild(nameNode);
    }

    private static int renameTaterzen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Component newName = MessageArgument.getMessage(context, "new name");

        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            taterzen.setCustomName(newName);
            context.getSource().sendSuccess(
                    successText("taterzens.command.rename.success", newName.getString()),
                    false
            );
        });
    }
}
