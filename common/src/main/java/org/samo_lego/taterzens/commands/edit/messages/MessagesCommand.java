package org.samo_lego.taterzens.commands.edit.messages;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.errorText;
import static org.samo_lego.taterzens.util.TextUtil.joinText;
import static org.samo_lego.taterzens.util.TextUtil.successText;
import static org.samo_lego.taterzens.util.TextUtil.translate;

public class MessagesCommand {
    public static LiteralCommandNode<CommandSourceStack> messagesNode;

    public static void registerNode(LiteralCommandNode<CommandSourceStack> editNode) {
        messagesNode = literal("messages")
                .then(literal("clear")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.messages.clear", config.perms.npcCommandPermissionLevel))
                        .executes(MessagesCommand::clearTaterzenMessages)
                )
                .then(literal("list")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.messages.list", config.perms.npcCommandPermissionLevel))
                        .executes(MessagesCommand::listTaterzenMessages)
                )
                .then(literal("swap")
                        .then(argument("id 1", IntegerArgumentType.integer())
                                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.messages.swap", config.perms.npcCommandPermissionLevel))
                                .then(argument("id 2", IntegerArgumentType.integer())
                                        .executes(MessagesCommand::swapMessages)
                                )
                        )
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.messages.reorder", config.perms.npcCommandPermissionLevel))
                )
                .then(argument("message id", IntegerArgumentType.integer())
                        .then(literal("delete")
                                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.messages.delete", config.perms.npcCommandPermissionLevel))
                                .executes(MessagesCommand::deleteTaterzenMessage)
                        )
                        .then(literal("setDelay")
                                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.messages.delay", config.perms.npcCommandPermissionLevel))
                                .then(argument("delay", IntegerArgumentType.integer(0))
                                        .executes(MessagesCommand::editMessageDelay)
                                )
                        )
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.messages", config.perms.npcCommandPermissionLevel))
                        .executes(MessagesCommand::editMessage)
                )
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.messages.edit", config.perms.npcCommandPermissionLevel))
                .executes(MessagesCommand::editTaterzenMessages).build();

        editNode.addChild(messagesNode);
    }

    private static int swapMessages(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        return NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
            int firstId = IntegerArgumentType.getInteger(context, "id 1");
            int secondId = IntegerArgumentType.getInteger(context, "id 2");

            ArrayList<Pair<Component, Integer>> messages = taterzen.getMessages();
            int size = messages.size();

            if (firstId < 0)
                firstId = Math.abs(size - firstId);
            if (secondId < 0)
                secondId = Math.abs(size - secondId);

            if(firstId > size || secondId > size) {
                source.sendFailure(
                        errorText("taterzens.command.message.error.404", firstId + " / " + secondId)
                );
                return;
            }

            --firstId;
            --secondId;

            Pair<Component, Integer> first = messages.remove(firstId);
            // - 1 as we remove one element above
            Pair<Component, Integer> second = messages.remove(secondId - 1);
            messages.add(firstId, second);
            messages.add(secondId, first);

            source.sendSuccess(translate("taterzens.command.message.swapped",
                    first.getFirst().copy().withStyle(ChatFormatting.YELLOW),
                    second.getFirst().copy().withStyle(ChatFormatting.YELLOW)
            ).withStyle(ChatFormatting.GREEN), false);
        });

    }

    private static int deleteTaterzenMessage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            final int selected = IntegerArgumentType.getInteger(context, "message id");

            List<Pair<Component, Integer>> messages = taterzen.getMessages();
            final int size = messages.size();

            final int actual = getMessageIndex(selected, size);

            if(actual == -1) {
                source.sendFailure(
                        errorText("taterzens.command.message.error.404", String.valueOf(selected))
                );
            } else {
                source.sendSuccess(successText("taterzens.command.message.deleted", taterzen.removeMessage(actual).getString()), false);
            }
        });
    }

    private static int editMessageDelay(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            final int selected = IntegerArgumentType.getInteger(context, "message id");

            List<Pair<Component, Integer>> messages = taterzen.getMessages();
            final int size = messages.size();

            final int actual = getMessageIndex(selected, size);

            if(actual == -1) {
                source.sendFailure(
                        errorText("taterzens.command.message.error.404", String.valueOf(selected))
                );
            } else {
                final int delay = IntegerArgumentType.getInteger(context, "delay");
                final int i = (actual - 1 + size) % size;
                String first = messages.get(i).getFirst().getString();
                String second = messages.get(actual).getFirst().getString();
                taterzen.setMessageDelay(actual, delay);
                source.sendSuccess(successText("taterzens.command.message.delay", first, second, String.valueOf(delay)), false);
            }
        });
    }

    /**
     * Gets message index from the list of messages.
     * @param selected selected message id (human-friendly, starting from 1)
     * @param size size of the list
     * @return index of the message in the list, or -1 if not found
     */
    private static int getMessageIndex(int selected, int size) {
        if (selected < 0)
            selected = size + selected;  // Backwards index
        else
            --selected;  // Programmer-formatted index

        if (selected >= size || selected < 0)
            selected = -1;

        return selected;
    }

    private static int editMessage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        return NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
            ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.MESSAGES);
            int selected = IntegerArgumentType.getInteger(context, "message id") - 1;
            if(selected >= taterzen.getMessages().size()) {
                source.sendSuccess(
                        successText("taterzens.command.message.list", String.valueOf(selected)),
                        false
                );
            } else {
                ((ITaterzenEditor) player).setEditingMessageIndex(selected);
                source.sendSuccess(
                        successText("taterzens.command.message.editor.enter", taterzen.getMessages().get(selected).getFirst().getString()),
                        false)
                ;
            }
        });
    }

    private static int listTaterzenMessages(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            ArrayList<Pair<Component, Integer>> messages = taterzen.getMessages();

            MutableComponent response = joinText("taterzens.command.message.list", ChatFormatting.AQUA, ChatFormatting.YELLOW, taterzen.getName().getString());
            AtomicInteger i = new AtomicInteger();

            messages.forEach(pair -> {
                int index = i.get() + 1;
                response.append(
                        Component.literal("\n" + index + "-> ")
                                .withStyle(index % 2 == 0 ? ChatFormatting.YELLOW : ChatFormatting.GOLD)
                                .append(pair.getFirst())
                                .withStyle(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit messages " + index))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.edit", index))
                                        ))
                )
                        .append("   ")
                        .append(
                                Component.literal("X")
                                        .withStyle(ChatFormatting.RED)
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(style -> style
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.delete", index)))
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc edit messages " + index + " delete"))
                                        )
                        );
                i.incrementAndGet();
            });
            source.sendSuccess(response, false);
        });
    }

    private static int clearTaterzenMessages(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            taterzen.clearMessages();
            source.sendSuccess(successText("taterzens.command.message.clear", taterzen.getName().getString()), false);
        });
    }

    private static int editTaterzenMessages(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        return NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
            if(((ITaterzenEditor) player).getEditorMode() == ITaterzenEditor.EditorMode.MESSAGES) {
                // Exiting the message edit mode
                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.NONE);
                ((ITaterzenEditor) player).setEditingMessageIndex(-1);
                source.sendSuccess(
                        translate("taterzens.command.equipment.exit").withStyle(ChatFormatting.LIGHT_PURPLE),
                        false
                );
            } else {
                // Entering the edit mode
                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.MESSAGES);
                source.sendSuccess(
                        joinText("taterzens.command.message.editor.enter", ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA, taterzen.getName().getString())
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit messages"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.exit").withStyle(ChatFormatting.RED)))
                                ),
                        false
                );
                source.sendSuccess(
                        successText("taterzens.command.message.editor.desc.1", taterzen.getName().getString())
                                .append("\n")
                                .append(translate("taterzens.command.message.editor.desc.2"))
                                .withStyle(ChatFormatting.GREEN),
                        false
                );
            }
        });
    }
}
