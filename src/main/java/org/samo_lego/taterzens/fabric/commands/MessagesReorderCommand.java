package org.samo_lego.taterzens.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.taterzens.common.commands.NpcCommand;
import org.samo_lego.taterzens.fabric.gui.MessagesEditGUI;

import static org.samo_lego.taterzens.common.commands.edit.messages.MessagesCommand.messagesNode;

public class MessagesReorderCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(((LiteralCommandNode<CommandSourceStack>) messagesNode.getChild("swap")).createBuilder().executes(MessagesReorderCommand::reorderMessages));
    }


    private static int reorderMessages(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        return NpcCommand.selectedTaterzenExecutor(player, taterzen ->
                new MessagesEditGUI(player, taterzen).open()
        );
    }
}
