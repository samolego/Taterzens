package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.joinText;
import static org.samo_lego.taterzens.util.TextUtil.successText;
import static org.samo_lego.taterzens.util.TextUtil.translate;

public class EquipmentCommand {

    public static void registerNode(LiteralCommandNode<CommandSourceStack> editNode) {
        LiteralCommandNode<CommandSourceStack> equipmentNode = literal("equipment")
                .then(literal("allowEquipmentDrops")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.equipment.drops", config.perms.npcCommandPermissionLevel))
                        .then(argument("drop", BoolArgumentType.bool()).executes(EquipmentCommand::setEquipmentDrops))
                )
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.equipment", config.perms.npcCommandPermissionLevel))
                .executes(EquipmentCommand::setEquipment)
                .build();

        editNode.addChild(equipmentNode);
    }

    static int setEquipmentDrops(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            boolean drop = BoolArgumentType.getBool(context, "drop");
            taterzen.allowEquipmentDrops(drop);
            source.sendSuccess(successText("taterzens.command.equipment.drop_mode.set", String.valueOf(drop)), false);
        });
    }

    private static int setEquipment(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            if(taterzen.isEquipmentEditor(player)) {
                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.NONE);
                taterzen.setEquipmentEditor(null);
                context.getSource().sendSuccess(
                        translate("taterzens.command.equipment.exit").withStyle(ChatFormatting.LIGHT_PURPLE),
                        false
                );

                taterzen.setEquipmentEditor(null);
            } else {
                source.sendSuccess(
                        joinText("taterzens.command.equipment.enter", ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA, taterzen.getName().getString())
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit equipment"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.exit").withStyle(ChatFormatting.RED)))
                                ),
                        false
                );
                source.sendSuccess(
                        translate("taterzens.command.equipment.desc.1").append("\n")
                                .append(translate("taterzens.command.equipment.desc.2")).append("\n")
                                .append(translate("taterzens.command.equipment.desc.3")).withStyle(ChatFormatting.YELLOW).append("\n")
                                .append(translate("taterzens.command.equipment.desc.4").withStyle(ChatFormatting.RED)),
                        false
                );

                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.EQUIPMENT);
                taterzen.setEquipmentEditor(player);
            }
        });
    }
}
