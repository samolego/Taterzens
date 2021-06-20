package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.util.Formatting;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class EquipmentCommand {

    public static void registerNode(LiteralCommandNode<ServerCommandSource> editNode) {
        LiteralCommandNode<ServerCommandSource> equipmentNode = literal("equipment")
                .then(literal("allowEquipmentDrops")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.equipment.drops", config.perms.npcCommandPermissionLevel))
                        .then(argument("drop", BoolArgumentType.bool()).executes(EquipmentCommand::setEquipmentDrops))
                )
                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.equipment", config.perms.npcCommandPermissionLevel))
                .executes(EquipmentCommand::setEquipment)
                .build();

        editNode.addChild(equipmentNode);
    }

    static int setEquipmentDrops(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();;
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            boolean drop = BoolArgumentType.getBool(context, "drop");
            taterzen.allowEquipmentDrops(drop);
            source.sendFeedback(successText("taterzens.command.equipment.drop_mode.set", String.valueOf(drop)), false);
        });
    }

    private static int setEquipment(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            if(taterzen.isEquipmentEditor(player)) {
                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.NONE);
                taterzen.setEquipmentEditor(null);
                context.getSource().sendFeedback(
                        translate("taterzens.command.equipment.exit").formatted(Formatting.LIGHT_PURPLE),
                        false
                );

                taterzen.setEquipmentEditor(null);
            } else {
                source.sendFeedback(
                        joinText("taterzens.command.equipment.enter", Formatting.LIGHT_PURPLE, Formatting.AQUA, taterzen.getName().getString())
                                .formatted(Formatting.BOLD)
                                .styled(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit equipment"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.exit").formatted(Formatting.RED)))
                                ),
                        false
                );
                source.sendFeedback(
                        translate("taterzens.command.equipment.desc.1").append("\n")
                                .append(translate("taterzens.command.equipment.desc.2")).append("\n")
                                .append(translate("taterzens.command.equipment.desc.3")).formatted(Formatting.YELLOW).append("\n")
                                .append(translate("taterzens.command.equipment.desc.4").formatted(Formatting.RED)),
                        false
                );

                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.EQUIPMENT);
                taterzen.setEquipmentEditor(player);
            }
        });
    }
}
