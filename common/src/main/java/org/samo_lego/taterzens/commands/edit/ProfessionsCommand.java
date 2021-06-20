package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.PROFESSION_TYPES;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class ProfessionsCommand {
    
    public static void registerNode(LiteralCommandNode<ServerCommandSource> editNode) {
        LiteralCommandNode<ServerCommandSource> professionsNode = literal("professions")
                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.professions", config.perms.npcCommandPermissionLevel))
                .then(literal("remove")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.professions.remove", config.perms.npcCommandPermissionLevel))
                        .then(argument("profession type", message())
                                .suggests(ProfessionsCommand::suggestRemovableProfessions)
                                .executes(ctx -> removeProfession(ctx, new Identifier(MessageArgumentType.getMessage(ctx, "profession type").asString())))
                        )
                )
                .then(literal("add")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.professions.add", config.perms.npcCommandPermissionLevel))
                        .then(argument("profession type", message())
                                .suggests((context, builder) -> CommandSource.suggestMatching(PROFESSION_TYPES.keySet().stream().map(Identifier::toString), builder))
                                .executes(ctx -> setProfession(ctx, new Identifier(MessageArgumentType.getMessage(ctx, "profession type").asString())))
                        )
                )
                .then(literal("list")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.professions.list", config.perms.npcCommandPermissionLevel))
                        .executes(ProfessionsCommand::listTaterzenProfessions)
                )
                .build();
        
        editNode.addChild(professionsNode);
    }


    private static int listTaterzenProfessions(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            Collection<Identifier> professionIds = taterzen.getProfessionIds();

            MutableText response = joinText("taterzens.command.profession.list", Formatting.AQUA, Formatting.YELLOW, taterzen.getName().getString());
            AtomicInteger i = new AtomicInteger();

            professionIds.forEach(identifier -> {
                int index = i.get() + 1;
                response.append(
                        new LiteralText("\n" + index + "-> " + identifier.toString() + " (")
                                .formatted(index % 2 == 0 ? Formatting.YELLOW : Formatting.GOLD)
                                .append(
                                        new LiteralText("X")
                                                .formatted(Formatting.RED)
                                                .formatted(Formatting.BOLD)
                                                .styled(style -> style
                                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.delete", identifier.getPath())))
                                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc edit professions remove " + identifier))
                                                )
                                )
                                .append(new LiteralText(")").formatted(Formatting.RESET))
                );
                i.incrementAndGet();
            });
            source.sendFeedback(response, false);
        });
    }

    private static int removeProfession(CommandContext<ServerCommandSource> context, Identifier id) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            if(taterzen.getProfessionIds().contains(id)) {
                taterzen.removeProfession(id);
                source.sendFeedback(successText("taterzens.command.profession.remove", id.toString()), false);
            } else
                context.getSource().sendError(errorText("taterzens.command.profession.error.404", id.toString()));
        });
    }

    private static int setProfession(CommandContext<ServerCommandSource> context, Identifier id) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            if(PROFESSION_TYPES.containsKey(id)) {
                taterzen.addProfession(id);
                source.sendFeedback(successText("taterzens.command.profession.add", id.toString()), false);
            } else
                context.getSource().sendError(errorText("taterzens.command.profession.error.404", id.toString()));
        });
    }

    private static CompletableFuture<Suggestions> suggestRemovableProfessions(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        Collection<Identifier> professions = new HashSet<>();
        try {
            TaterzenNPC taterzen = ((ITaterzenEditor) ctx.getSource().getPlayer()).getNpc();
            if(taterzen != null) {
                professions = taterzen.getProfessionIds();
            }
        } catch(CommandSyntaxException ignored) {
        }
        return CommandSource.suggestMatching(professions.stream().map(Identifier::toString), builder);
    }
}
