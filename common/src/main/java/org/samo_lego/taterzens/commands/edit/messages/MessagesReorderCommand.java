package org.samo_lego.taterzens.commands.edit.messages;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.gui.MessagesEditGUI;

import static org.samo_lego.taterzens.commands.edit.messages.MessagesCommand.messagesNode;

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
