package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.samo_lego.taterzens.commands.NpcCommand;

import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class NameCommand {

    public static void registerNode(LiteralCommandNode<ServerCommandSource> editNode) {
        LiteralCommandNode<ServerCommandSource> nameNode = literal("name")
                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.name", config.perms.npcCommandPermissionLevel))
                .then(argument("new name", message()).executes(NameCommand::renameTaterzen))
                .build();

        editNode.addChild(nameNode);
    }

    private static int renameTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Text newName = MessageArgumentType.getMessage(context, "new name");

        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            taterzen.setCustomName(newName);
            context.getSource().sendFeedback(
                    successText("taterzens.command.rename.success", newName.getString()),
                    false
            );
        });
    }
}
