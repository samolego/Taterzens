package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.taterzens.gui.EditorGUI;

import java.util.Arrays;
import java.util.Collections;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static org.samo_lego.taterzens.commands.NpcCommand.npcNode;

public class NpcGUICommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        dispatcher.register(npcNode.createBuilder().executes(NpcGUICommand::openGUI));

        ArgumentCommandNode<CommandSourceStack, String> build = argument("", word()).build();
    }

    private static int openGUI(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        new EditorGUI(context, player,null, Collections.singletonList(npcNode)).open();
        return 0;
    }
}
