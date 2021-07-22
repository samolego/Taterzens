package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.samo_lego.taterzens.commands.NpcCommand;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.commands.NpcCommand.noSelectedTaterzenError;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class CommandsCommand {
    public static void registerNode(CommandDispatcher<ServerCommandSource> dispatcher, LiteralCommandNode< ServerCommandSource > editNode) {
        LiteralCommandNode<ServerCommandSource> commandsNode = literal("commands")
                .then(literal("setPermissionLevel")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.commands.set_permission_level", config.perms.npcCommandPermissionLevel))
                        .then(argument("level", IntegerArgumentType.integer(0, 4))
                                .executes(CommandsCommand::setPermissionLevel)
                        )
                )
                .then(literal("remove")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.commands.remove", config.perms.npcCommandPermissionLevel))
                        .then(argument("command id", IntegerArgumentType.integer(0)).executes(CommandsCommand::removeCommand))
                )
                .then(literal("addBuiltin")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.commands.add", config.perms.npcCommandPermissionLevel))
                        .redirect(dispatcher.getRoot(), context -> {
                            // Really ugly, but ... works :P
                            String cmd = addCommand(context);
                            throw new SimpleCommandExceptionType(
                                    cmd == null ?
                                            noSelectedTaterzenError() :
                                            joinText("taterzens.command.commands.set", Formatting.GOLD, Formatting.GRAY, "/" + cmd)
                            ).create();
                        })
                )
                .then(literal("addCustom")
                        .then(argument("velocity command", MessageArgumentType.message())
                                .executes(CommandsCommand::addVelocityCommand)
                        )
                )

                .then(literal("clear")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.commands.clear", config.perms.npcCommandPermissionLevel))
                        .executes(CommandsCommand::clearCommands)
                )
                .then(literal("list")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.commands.list", config.perms.npcCommandPermissionLevel))
                        .executes(CommandsCommand::listTaterzenCommands)
                )
                .build();
        
        editNode.addChild(commandsNode);
    }


    private static int removeCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        int selected = IntegerArgumentType.getInteger(context, "command id") - 1;

        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            if(selected >= taterzen.getCommands().size()) {
                source.sendFeedback(
                        errorText("taterzens.command.commands.error.404", String.valueOf(selected)),
                        false
                );
            } else {
                source.sendFeedback(successText("taterzens.command.commands.removed", taterzen.getCommands().get(selected)), false);
                taterzen.removeCommand(selected);
            }
        });
    }

    private static int clearCommands(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            source.sendFeedback(successText("taterzens.command.commands.cleared", taterzen.getName().getString()), false);
            taterzen.clearCommands();

        });
    }

    private static int listTaterzenCommands(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            ArrayList<String> commands = taterzen.getCommands();

            MutableText response = joinText("taterzens.command.commands.list", Formatting.AQUA, Formatting.YELLOW, taterzen.getName().getString());
            if(!commands.isEmpty()) {
                AtomicInteger i = new AtomicInteger();

                commands.forEach(cmd -> {
                    int index = i.get() + 1;
                    response.append(
                            new LiteralText("\n" + index + "-> ")
                                    .formatted(index % 2 == 0 ? Formatting.YELLOW : Formatting.GOLD)
                                    .append(cmd)
                                    .append("   ")
                                    .append(
                                            new LiteralText("X")
                                                    .formatted(Formatting.RED)
                                                    .formatted(Formatting.BOLD)
                                                    .styled(style -> style
                                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.delete", index)))
                                                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc edit commands remove " + index))
                                                    )
                                    )
                    );
                    i.incrementAndGet();
                });
            }

            source.sendFeedback(response, false);
        });
    }

    private static int setPermissionLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();;
        int newPermLevel = IntegerArgumentType.getInteger(context, "level");

        if(!config.perms.allowSettingHigherPermissionLevel && !source.hasPermissionLevel(newPermLevel)) {
            source.sendError(errorText("taterzens.error.permission", String.valueOf(newPermLevel)));
            return -1;
        }

        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            source.sendFeedback(successText("taterzens.command.commands.permission.set", String.valueOf(newPermLevel)), false);
            taterzen.setPermissionLevel(newPermLevel);

        });
    }

    private static int addVelocityCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String command = MessageArgumentType.getMessage(context, "velocity command").getString();

        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            //todo
            //source.sendFeedback(successText("taterzens.command.commands.set", command), false);
        });

    }

    private static String addCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        AtomicReference<String> command = new AtomicReference<>();
        NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            // Extremely :concern:
            // I know it
            command.set(context.getInput().substring("/npc edit commands addBuiltin ".length()));
            taterzen.addCommand(command.get());
        });
        return command.get();
    }
}
