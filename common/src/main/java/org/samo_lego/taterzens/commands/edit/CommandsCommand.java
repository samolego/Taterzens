package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Triple;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.compatibility.BungeeCommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.MODID;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.commands.NpcCommand.noSelectedTaterzenError;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class CommandsCommand {
    private static final SuggestionProvider<ServerCommandSource> BUNGEE_COMMANDS;
    private static final SuggestionProvider<ServerCommandSource> BUNGEE_SERVERS;
    private static final SuggestionProvider<ServerCommandSource> PLAYERS;

    public static void registerNode(CommandDispatcher<ServerCommandSource> dispatcher, LiteralCommandNode<ServerCommandSource> editNode) {
        LiteralCommandNode<ServerCommandSource> commandsNode = literal("commands")
                .then(literal("setPermissionLevel")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.commands.set_permission_level", config.perms.npcCommandPermissionLevel))
                        .then(argument("level", IntegerArgumentType.integer(0, 4))
                                .executes(CommandsCommand::setPermissionLevel)
                        )
                )
                .then(literal("remove")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.commands.remove", config.perms.npcCommandPermissionLevel))
                        .then(argument("command id", IntegerArgumentType.integer()).executes(CommandsCommand::removeCommand))
                )
                .then(literal("add")
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
                .then(literal("addBungee")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.commands.addBungee", config.perms.npcCommandPermissionLevel))
                        .then(argument("command", string())
                            .suggests(BUNGEE_COMMANDS)
                            .then(argument("player", string())
                                .suggests(PLAYERS)
                                .then(argument("argument", string())
                                    .suggests(BUNGEE_SERVERS)
                                    .executes(CommandsCommand::addBungeeCommand)
                                )
                            )
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
        int original = IntegerArgumentType.getInteger(context, "command id");
        int selected = original;
        boolean bungee = false;

        if(selected < 0) {
            selected *= -1;
            bungee = true;
        }
        --selected;

        int finalSelected = selected;
        boolean finalBungee = bungee;
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrThrow(), taterzen -> {
            ArrayList<?> cmds = finalBungee ? taterzen.getBungeeCommands() : taterzen.getCommands();
            if(finalSelected >= cmds.size()) {
                source.sendFeedback(
                        errorText("taterzens.command.commands.error.404", String.valueOf(original)),
                        false
                );
            } else {
                source.sendFeedback(successText("taterzens.command.commands.removed", cmds.get(finalSelected).toString()), false);
                taterzen.removeCommand(finalSelected, finalBungee);
            }
        });
    }

    private static int clearCommands(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrThrow(), taterzen -> {
            source.sendFeedback(successText("taterzens.command.commands.cleared", taterzen.getName().getString()), false);
            taterzen.clearCommands(true);
            taterzen.clearCommands(false);
        });
    }

    private static int listTaterzenCommands(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrThrow(), taterzen -> {
            ArrayList<String> commands = taterzen.getCommands();
            ArrayList<Triple<BungeeCommands, String, String>> bungeeCommands = taterzen.getBungeeCommands();
            final String separator = "\n-------------------------------------------------";

            MutableText response = joinText("taterzens.command.commands.list", Formatting.AQUA, Formatting.YELLOW, taterzen.getName().getString());
            response.append(separator);
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
            if(!bungeeCommands.isEmpty()) {
                response.append(separator + "\n");
                response.append(translate("taterzens.command.commands.listBungee").formatted(Formatting.AQUA));

                if(!config.bungee.enableCommands) {
                    response.append("\n");
                    response.append(errorText("taterzens.error.enableBungee").formatted(Formatting.ITALIC));
                }

                response.append(separator);

                AtomicInteger c = new AtomicInteger();
                bungeeCommands.forEach(cmd -> {
                    int index = c.get() - 1;
                    response.append(
                            new LiteralText("\n" + index + "-> ")
                                    .formatted(index % 2 == 0 ? Formatting.DARK_GREEN : Formatting.BLUE)
                                    .append(cmd.getLeft() + " " + cmd.getMiddle() + " " + cmd.getRight())
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
                    c.decrementAndGet();
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

        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrThrow(), taterzen -> {
            source.sendFeedback(successText("taterzens.command.commands.permission.set", String.valueOf(newPermLevel)), false);
            taterzen.setPermissionLevel(newPermLevel);

        });
    }

    private static int addBungeeCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String command = StringArgumentType.getString(context, "command");
        String player = StringArgumentType.getString(context, "player");
        String argument = "";
        try {
            argument = StringArgumentType.getString(context, "argument");
        } catch (IllegalArgumentException ignored) {
        }

        String finalArgument = argument;
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrThrow(), taterzen -> {
            boolean added = taterzen.addBungeeCommand(BungeeCommands.valueOf(command.toUpperCase()), player, finalArgument);
            Text text;
            if(added)
                text = joinText("taterzens.command.commands.setBungee",
                    Formatting.GOLD,
                    Formatting.GRAY,
                    "/" + command + " " + player + " " + finalArgument
                );
            else
                text = errorText("taterzens.error.enableBungee");
            source.sendFeedback(text, false);
        });
    }

    private static String addCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        AtomicReference<String> command = new AtomicReference<>();
        NpcCommand.selectedTaterzenExecutor(source.getEntityOrThrow(), taterzen -> {
            // Extremely :concern:
            // I know it
            String inputCmd = context.getInput();
            String index = "add ";
            command.set(inputCmd.substring(inputCmd.indexOf(index) + index.length()));
            taterzen.addCommand(command.get());
        });
        return command.get();
    }


    static {
        BUNGEE_COMMANDS = SuggestionProviders.register(
                new Identifier(MODID, "bungee_commands"),
                (context, builder) ->
                        CommandSource.suggestMatching(Stream.of(BungeeCommands.values()).map(cmd -> cmd.toString().toLowerCase()).collect(Collectors.toList()), builder)
        );

        BUNGEE_SERVERS = SuggestionProviders.register(
                new Identifier(MODID, "bungee_servers"),
                (context, builder) -> {
                    try {
                        String command = StringArgumentType.getString(context, "command");
                        if(BungeeCommands.valueOf(command.toUpperCase()) != BungeeCommands.SERVER)
                            return builder.buildFuture();
                    } catch (IllegalArgumentException ignored) {
                    }

                    return CommandSource.suggestMatching(config.bungee.servers, builder);
                }
        );
        PLAYERS = SuggestionProviders.register(
                new Identifier(MODID, "players"),
                (context, builder) -> {
                    Collection<String> names = context.getSource().getPlayerNames();
                    names.add("--clicker--");
                    return CommandSource.suggestMatching(names, builder);
                }
        );
    }
}
