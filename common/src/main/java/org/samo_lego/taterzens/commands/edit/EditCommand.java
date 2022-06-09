package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.samo_lego.taterzens.commands.edit.commands.CommandsCommand;
import org.samo_lego.taterzens.commands.edit.messages.MessagesCommand;

import static net.minecraft.commands.Commands.literal;

public class EditCommand {

    public static void registerNode(CommandDispatcher<CommandSourceStack> dispatcher, LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> editNode = literal("edit")
                .build();

        npcNode.addChild(editNode);

        // Other sub nodes from "edit"
        BehaviourCommand.registerNode(editNode);
        CommandsCommand.registerNode(dispatcher, editNode);
        EquipmentCommand.registerNode(editNode);
        MessagesCommand.registerNode(editNode);
        MountCommand.registerNode(editNode);
        MovementCommand.registerNode(editNode);
        NameCommand.registerNode(editNode);
        PathCommand.registerNode(editNode);
        PoseCommand.registerNode(editNode);
        ProfessionsCommand.registerNode(editNode);
        RespawnPointCommand.registerNode(editNode);
        SkinCommand.registerNode(editNode);
        SoundCommand.registerNode(editNode);
        TagsCommand.registerNode(editNode);
        TypeCommand.registerNode(editNode);
    }
}
