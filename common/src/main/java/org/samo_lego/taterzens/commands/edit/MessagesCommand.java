package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class MessagesCommand {

    public static void registerNode(LiteralCommandNode<ServerCommandSource> editNode) {
        LiteralCommandNode<ServerCommandSource> messagesNode = literal("messages")
                .then(literal("clear")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.messages.clear", config.perms.npcCommandPermissionLevel))
                        .executes(MessagesCommand::clearTaterzenMessages)
                )
                .then(literal("list")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.messages.list", config.perms.npcCommandPermissionLevel))
                        .executes(MessagesCommand::listTaterzenMessages)
                )
                .then(argument("message id", IntegerArgumentType.integer(0))
                        .then(literal("delete")
                                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.messages.delete", config.perms.npcCommandPermissionLevel))
                                .executes(MessagesCommand::deleteTaterzenMessage)
                        )
                        .then(literal("setDelay")
                                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.messages.delay", config.perms.npcCommandPermissionLevel))
                                .then(argument("delay", IntegerArgumentType.integer())
                                        .executes(MessagesCommand::editMessageDelay)
                                )
                        )
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.messages", config.perms.npcCommandPermissionLevel))
                        .executes(MessagesCommand::editMessage)
                )
                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.messages.edit", config.perms.npcCommandPermissionLevel))
                .executes(MessagesCommand::editTaterzenMessages).build();
        
        editNode.addChild(messagesNode);
    }

    private static int deleteTaterzenMessage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            int selected = IntegerArgumentType.getInteger(context, "message id") - 1;
            if(selected >= taterzen.getMessages().size()) {
                source.sendFeedback(
                        errorText("taterzens.command.message.error.404", String.valueOf(selected)),
                        false
                );
            } else {
                source.sendFeedback(successText("taterzens.command.message.deleted", taterzen.getMessages().get(selected).getFirst().getString()), false);
                taterzen.removeMessage(selected);
            }
        });
    }

    private static int editMessageDelay(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            int selected = IntegerArgumentType.getInteger(context, "message id") - 1;
            if(selected >= taterzen.getMessages().size()) {
                source.sendFeedback(
                        errorText("taterzens.command.message.error.404", String.valueOf(selected)),
                        false
                );
            } else {
                int delay = IntegerArgumentType.getInteger(context, "delay");
                taterzen.setMessageDelay(selected, delay);
                source.sendFeedback(successText("taterzens.command.message.delay", String.valueOf(delay)), false);
            }
        });
    }

    private static int editMessage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        return NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
            ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.MESSAGES);
            int selected = IntegerArgumentType.getInteger(context, "message id") - 1;
            if(selected >= taterzen.getMessages().size()) {
                source.sendFeedback(
                        successText("taterzens.command.message.list", String.valueOf(selected)),
                        false
                );
            } else {
                ((ITaterzenEditor) player).setEditingMessageIndex(selected);
                source.sendFeedback(
                        successText("taterzens.command.message.editor.enter", taterzen.getMessages().get(selected).getFirst().getString()),
                        false)
                ;
            }
        });
    }

    private static int listTaterzenMessages(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            ArrayList<Pair<Text, Integer>> messages = taterzen.getMessages();

            MutableText response = joinText("taterzens.command.message.list", Formatting.AQUA, Formatting.YELLOW, taterzen.getName().getString());
            AtomicInteger i = new AtomicInteger();

            messages.forEach(pair -> {
                int index = i.get() + 1;
                response.append(
                        new LiteralText("\n" + index + "-> ")
                                .formatted(index % 2 == 0 ? Formatting.YELLOW : Formatting.GOLD)
                                .append(pair.getFirst())
                                .styled(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit messages " + index))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.edit", index))
                                        ))
                )
                        .append("   ")
                        .append(
                                new LiteralText("X")
                                        .formatted(Formatting.RED)
                                        .formatted(Formatting.BOLD)
                                        .styled(style -> style
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.delete", index)))
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc edit messages " + index + " delete"))
                                        )
                        );
                i.incrementAndGet();
            });
            source.sendFeedback(response, false);
        });
    }

    private static int clearTaterzenMessages(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            taterzen.clearMessages();
            source.sendFeedback(successText("taterzens.command.message.clear", taterzen.getName().getString()), false);
        });
    }

    private static int editTaterzenMessages(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        return NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
            if(((ITaterzenEditor) player).getEditorMode() == ITaterzenEditor.EditorMode.MESSAGES) {
                // Exiting the message edit mode
                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.NONE);
                ((ITaterzenEditor) player).setEditingMessageIndex(-1);
                source.sendFeedback(
                        translate("taterzens.command.equipment.exit").formatted(Formatting.LIGHT_PURPLE),
                        false
                );
            } else {
                // Entering the edit mode
                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.MESSAGES);
                source.sendFeedback(
                        joinText("taterzens.command.message.editor.enter", Formatting.LIGHT_PURPLE, Formatting.AQUA, taterzen.getName().getString())
                                .formatted(Formatting.BOLD)
                                .styled(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit messages"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.exit").formatted(Formatting.RED)))
                                ),
                        false
                );
                source.sendFeedback(
                        successText("taterzens.command.message.editor.desc.1", taterzen.getName().getString())
                                .append("\n")
                                .append(translate("taterzens.command.message.editor.desc.2"))
                                .formatted(Formatting.GREEN),
                        false
                );
            }
        });
    }
}
