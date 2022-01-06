package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;

import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.joinText;
import static org.samo_lego.taterzens.util.TextUtil.successText;
import static org.samo_lego.taterzens.util.TextUtil.translate;

public class PathCommand {
    public static void registerNode(LiteralCommandNode<CommandSourceStack> editNode) {
        LiteralCommandNode<CommandSourceStack> pathNode = literal("path")
                .then(literal("clear")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.path.clear", config.perms.npcCommandPermissionLevel))
                        .executes(PathCommand::clearTaterzenPath)
                )
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.path", config.perms.npcCommandPermissionLevel))
                .executes(PathCommand::editTaterzenPath)
                .build();

        editNode.addChild(pathNode);
    }

    private static int clearTaterzenPath(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Entity entity = source.getEntityOrException();

        return NpcCommand.selectedTaterzenExecutor(entity, taterzen -> {
            Level world = entity.getCommandSenderWorld();
            if(entity instanceof ServerPlayer player)
                taterzen.getPathTargets().forEach(blockPos -> player.connection.send(
                        new ClientboundBlockUpdatePacket(blockPos, world.getBlockState(blockPos))
                ));
            taterzen.clearPathTargets();
            context.getSource().sendSuccess(
                    successText("taterzens.command.path_editor.clear", taterzen.getName().getString()),
                    false
            );
        });
    }

    private static int editTaterzenPath(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        return NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
            if(((ITaterzenEditor) player).getEditorMode() == ITaterzenEditor.EditorMode.PATH) {
                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.NONE);
                source.sendSuccess(
                        translate("taterzens.command.equipment.exit").withStyle(ChatFormatting.LIGHT_PURPLE),
                        false
                );

            } else {

                source.sendSuccess(
                        joinText("taterzens.command.path_editor.enter", ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA, taterzen.getName().getString())
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit path"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.exit").withStyle(ChatFormatting.RED)))
                                ),
                        false
                );
                source.sendSuccess(
                        translate("taterzens.command.path_editor.desc.1").append("\n").withStyle(ChatFormatting.BLUE)
                                .append(translate("taterzens.command.path_editor.desc.2").withStyle(ChatFormatting.RED)),
                        false
                );

                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.PATH);
            }

        });
    }

}
