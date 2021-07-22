package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class EditCommand {

    public static void registerNode(CommandDispatcher<ServerCommandSource> dispatcher, LiteralCommandNode<ServerCommandSource> npcNode) {
        LiteralCommandNode<ServerCommandSource> editNode = literal("edit")
                .build();

        npcNode.addChild(editNode);

        // Other sub nodes from "edit"
        BehaviourCommand.registerNode(editNode);
        CommandsCommand.registerNode(dispatcher, editNode);
        EquipmentCommand.registerNode(editNode);
        MessagesCommand.registerNode(editNode);
        MovementCommand.registerNode(editNode);
        NameCommand.registerNode(editNode);
        PathCommand.registerNode(editNode);
        PoseCommand.registerNode(editNode);
        MountCommand.registerNode(editNode);
        ProfessionsCommand.registerNode(editNode);
        SkinCommand.registerNode(editNode);
        TagsCommand.registerNode(editNode);
        TypeCommand.registerNode(editNode);
    }
}
