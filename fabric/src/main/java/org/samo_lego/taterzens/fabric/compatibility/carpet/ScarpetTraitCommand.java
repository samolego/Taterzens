package org.samo_lego.taterzens.fabric.compatibility.carpet;

import carpet.script.value.Value;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.api.professions.TaterzenProfession;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.commands.ProfessionCommand.PROFESSION_COMMAND_NODE;
import static org.samo_lego.taterzens.compatibility.ModDiscovery.CARPETMOD_LOADED;
import static org.samo_lego.taterzens.util.TextUtil.errorText;
import static org.samo_lego.taterzens.util.TextUtil.joinText;
import static org.samo_lego.taterzens.util.TextUtil.successText;
import static org.samo_lego.taterzens.util.TextUtil.translate;

public class ScarpetTraitCommand {
    public static void register() {
        LiteralCommandNode<CommandSourceStack> scarpet = literal("scarpetTraits")
                .requires(src -> CARPETMOD_LOADED && Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.profession.scarpet", config.perms.professionCommandPL))
                .then(literal("add")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.profession.scarpet.add", config.perms.professionCommandPL))
                        .then(argument("id", StringArgumentType.word())
                                .executes(ScarpetTraitCommand::addTrait)
                        )
                )
                .then(literal("list")
                        .executes(ScarpetTraitCommand::listScarpetTraits)
                )
                .then(literal("remove")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.profession.scarpet.remove", config.perms.professionCommandPL))
                        .then(argument("id", StringArgumentType.word())
                                .suggests(ScarpetTraitCommand::suggestRemovableTraits)
                                .executes(ScarpetTraitCommand::removeTrait)
                        )
                )
                .executes(ScarpetTraitCommand::listScarpetTraits)
                .build();


        PROFESSION_COMMAND_NODE.addChild(scarpet);
    }


    private static int listScarpetTraits(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            TaterzenProfession profession = taterzen.getProfession(ScarpetProfession.ID);
            if (profession instanceof ScarpetProfession scarpetProfession) {
                HashSet<Value> traitIds = scarpetProfession.getTraits();

                MutableComponent response = joinText("taterzens.command.trait.list", ChatFormatting.AQUA, ChatFormatting.YELLOW, taterzen.getName().getString());
                AtomicInteger i = new AtomicInteger();

                traitIds.forEach(trait -> {
                    int index = i.get() + 1;

                    String id = trait.getString();
                    response.append(
                            Component.literal("\n" + index + "-> " + id + " (")
                                    .withStyle(index % 2 == 0 ? ChatFormatting.YELLOW : ChatFormatting.GOLD)
                                    .append(
                                            Component.literal("X")
                                                    .withStyle(ChatFormatting.RED)
                                                    .withStyle(ChatFormatting.BOLD)
                                                    .withStyle(style -> style
                                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.delete", id)))
                                                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/trait scarpet remove " + id))
                                                    )
                                    )
                                    .append(Component.literal(")").withStyle(ChatFormatting.RESET))
                    );
                    i.incrementAndGet();
                });
                source.sendSuccess(response, false);
            } else {
                source.sendFailure(errorText("taterzens.profession.lacking", ScarpetProfession.ID.toString()));
            }
        });
    }

    private static int removeTrait(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String id = StringArgumentType.getString(context, "id");
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            TaterzenProfession profession = taterzen.getProfession(ScarpetProfession.ID);
            if (profession instanceof ScarpetProfession scarpetProfession) {
                if (scarpetProfession.removeTrait(id))
                    source.sendSuccess(successText("taterzens.command.trait.remove", id), false);
                else
                    context.getSource().sendFailure(errorText("taterzens.command.trait.error.404", id));
            } else {
                source.sendFailure(errorText("taterzens.profession.lacking", ScarpetProfession.ID.toString()));
            }
        });
    }

    private static int addTrait(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String id = StringArgumentType.getString(context, "id");
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            TaterzenProfession profession = taterzen.getProfession(ScarpetProfession.ID);
            if (profession instanceof ScarpetProfession scarpetProfession) {
                scarpetProfession.addTrait(id);
                source.sendSuccess(successText("taterzens.command.trait.add", id), false);
            } else {
                source.sendFailure(errorText("taterzens.profession.lacking", ScarpetProfession.ID.toString()));
            }
        });
    }

    private static CompletableFuture<Suggestions> suggestRemovableTraits(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        HashSet<Value> traits = new HashSet<>();
        try {
            TaterzenNPC taterzen = ((ITaterzenEditor) ctx.getSource().getPlayerOrException()).getNpc();
            if(taterzen != null && taterzen.getProfession(ScarpetProfession.ID) instanceof ScarpetProfession scarpetProfession) {
                traits = scarpetProfession.getTraits();
            }
        } catch(CommandSyntaxException ignored) { }
        return SharedSuggestionProvider.suggest(traits.stream().map(Value::getPrettyString), builder);
    }
}
