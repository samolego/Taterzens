package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.taterzens.Taterzens;

import static org.samo_lego.taterzens.commands.NpcCommand.npcNode;

public class NpcGUICommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(npcNode.createBuilder().executes(NpcGUICommand::openGUI));
    }

    private static int openGUI(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Taterzens.getInstance().getPlatform().openEditorGui(player);
        return 1;
    }
}
