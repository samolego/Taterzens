package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.compatibility.BungeeCompatibility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.MODID;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.commands.NpcCommand.noSelectedTaterzenError;
import static org.samo_lego.taterzens.compatibility.BungeeCompatibility.AVAILABLE_SERVERS;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class CommandsCommand {
    private static final SuggestionProvider<CommandSourceStack> BUNGEE_COMMANDS;
    private static final SuggestionProvider<CommandSourceStack> PLAYERS;

    public static void registerNode(CommandDispatcher<CommandSourceStack> dispatcher, LiteralCommandNode<CommandSourceStack> editNode) {
        LiteralCommandNode<CommandSourceStack> commandsNode = literal("commands")
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
                                            joinText("taterzens.command.commands.set", ChatFormatting.GOLD, ChatFormatting.GRAY, "/" + cmd)
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
                                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(AVAILABLE_SERVERS, builder))
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


    private static int removeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
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
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            ArrayList<?> cmds = finalBungee ? taterzen.getBungeeCommands() : taterzen.getCommands();
            if(finalSelected >= cmds.size()) {
                source.sendSuccess(
                        errorText("taterzens.command.commands.error.404", String.valueOf(original)),
                        false
                );
            } else {
                source.sendSuccess(successText("taterzens.command.commands.removed", cmds.get(finalSelected).toString()), false);
                taterzen.removeCommand(finalSelected, finalBungee);
            }
        });
    }

    private static int clearCommands(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            source.sendSuccess(successText("taterzens.command.commands.cleared", taterzen.getName().getString()), false);
            taterzen.clearCommands(true);
            taterzen.clearCommands(false);
        });
    }

    private static int listTaterzenCommands(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            ArrayList<String> commands = taterzen.getCommands();
            ArrayList<Triple<BungeeCompatibility, String, String>> bungeeCommands = taterzen.getBungeeCommands();
            final String separator = "\n-------------------------------------------------";

            MutableComponent response = joinText("taterzens.command.commands.list", ChatFormatting.AQUA, ChatFormatting.YELLOW, taterzen.getName().getString());
            response.append(separator);
            if(!commands.isEmpty()) {
                AtomicInteger i = new AtomicInteger();

                commands.forEach(cmd -> {
                    int index = i.get() + 1;
                    response.append(
                            new TextComponent("\n" + index + "-> ")
                                    .withStyle(index % 2 == 0 ? ChatFormatting.YELLOW : ChatFormatting.GOLD)
                                    .append(cmd)
                                    .append("   ")
                                    .append(
                                            new TextComponent("X")
                                                    .withStyle(ChatFormatting.RED)
                                                    .withStyle(ChatFormatting.BOLD)
                                                    .withStyle(style -> style
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
                response.append(translate("taterzens.command.commands.listBungee").withStyle(ChatFormatting.AQUA));

                if(!config.bungee.enableCommands) {
                    response.append("\n");
                    response.append(errorText("taterzens.error.enableBungee").withStyle(ChatFormatting.ITALIC));
                }

                response.append(separator);

                AtomicInteger c = new AtomicInteger();
                bungeeCommands.forEach(cmd -> {
                    int index = c.get() - 1;
                    response.append(
                            new TextComponent("\n" + index + "-> ")
                                    .withStyle(index % 2 == 0 ? ChatFormatting.DARK_GREEN : ChatFormatting.BLUE)
                                    .append(cmd.getLeft() + " " + cmd.getMiddle() + " " + cmd.getRight())
                                    .append("   ")
                                    .append(
                                            new TextComponent("X")
                                                    .withStyle(ChatFormatting.RED)
                                                    .withStyle(ChatFormatting.BOLD)
                                                    .withStyle(style -> style
                                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.delete", index)))
                                                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc edit commands remove " + index))
                                                    )
                                    )
                    );
                    c.decrementAndGet();
                });
            }
            source.sendSuccess(response, false);
        });
    }

    private static int setPermissionLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();;
        int newPermLevel = IntegerArgumentType.getInteger(context, "level");

        if(!config.perms.allowSettingHigherPermissionLevel && !source.hasPermission(newPermLevel)) {
            source.sendFailure(errorText("taterzens.error.permission", String.valueOf(newPermLevel)));
            return -1;
        }

        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            source.sendSuccess(successText("taterzens.command.commands.permission.set", String.valueOf(newPermLevel)), false);
            taterzen.setPermissionLevel(newPermLevel);

        });
    }

    private static int addBungeeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String command = StringArgumentType.getString(context, "command");
        String player = StringArgumentType.getString(context, "player");
        String argument = "";
        try {
            argument = StringArgumentType.getString(context, "argument");
        } catch (IllegalArgumentException ignored) {
        }

        String finalArgument = argument;
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            boolean added = taterzen.addBungeeCommand(BungeeCompatibility.valueOf(command.toUpperCase()), player, finalArgument);
            Component text;
            if(added)
                text = joinText("taterzens.command.commands.setBungee",
                    ChatFormatting.GOLD,
                    ChatFormatting.GRAY,
                    "/" + command + " " + player + " " + finalArgument
                );
            else
                text = errorText("taterzens.error.enableBungee");
            source.sendSuccess(text, false);
        });
    }

    private static String addCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        AtomicReference<String> command = new AtomicReference<>();
        NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
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
                new ResourceLocation(MODID, "bungee_commands"),
                (context, builder) ->
                        SharedSuggestionProvider.suggest(Stream.of(BungeeCompatibility.values()).map(cmd -> cmd.toString().toLowerCase()).collect(Collectors.toList()), builder)
        );
        PLAYERS = SuggestionProviders.register(
                new ResourceLocation(MODID, "players"),
                (context, builder) -> {
                    Collection<String> names = context.getSource().getOnlinePlayerNames();
                    names.add("--clicker--");
                    return SharedSuggestionProvider.suggest(names, builder);
                }
        );
    }
}
