package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.commands.arguments.MessageArgument.message;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.PROFESSION_TYPES;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class ProfessionsCommand {
    
    public static void registerNode(LiteralCommandNode<CommandSourceStack> editNode) {
        LiteralCommandNode<CommandSourceStack> professionsNode = literal("professions")
                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.professions", config.perms.npcCommandPermissionLevel))
                .then(literal("remove")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.professions.remove", config.perms.npcCommandPermissionLevel))
                        .then(argument("profession type", message())
                                .suggests(ProfessionsCommand::suggestRemovableProfessions)
                                .executes(ctx -> removeProfession(ctx, new ResourceLocation(MessageArgument.getMessage(ctx, "profession type").getContents())))
                        )
                )
                .then(literal("add")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.professions.add", config.perms.npcCommandPermissionLevel))
                        .then(argument("profession type", message())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(PROFESSION_TYPES.keySet().stream().map(ResourceLocation::toString), builder))
                                .executes(ctx -> setProfession(ctx, new ResourceLocation(MessageArgument.getMessage(ctx, "profession type").getContents())))
                        )
                )
                .then(literal("list")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.professions.list", config.perms.npcCommandPermissionLevel))
                        .executes(ProfessionsCommand::listTaterzenProfessions)
                )
                .build();
        
        editNode.addChild(professionsNode);
    }


    private static int listTaterzenProfessions(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            Collection<ResourceLocation> professionIds = taterzen.getProfessionIds();

            MutableComponent response = joinText("taterzens.command.profession.list", ChatFormatting.AQUA, ChatFormatting.YELLOW, taterzen.getName().getString());
            AtomicInteger i = new AtomicInteger();

            professionIds.forEach(ResourceLocation -> {
                int index = i.get() + 1;
                response.append(
                        new TextComponent("\n" + index + "-> " + ResourceLocation.toString() + " (")
                                .withStyle(index % 2 == 0 ? ChatFormatting.YELLOW : ChatFormatting.GOLD)
                                .append(
                                        new TextComponent("X")
                                                .withStyle(ChatFormatting.RED)
                                                .withStyle(ChatFormatting.BOLD)
                                                .withStyle(style -> style
                                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.delete", ResourceLocation.getPath())))
                                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc edit professions remove " + ResourceLocation))
                                                )
                                )
                                .append(new TextComponent(")").withStyle(ChatFormatting.RESET))
                );
                i.incrementAndGet();
            });
            source.sendSuccess(response, false);
        });
    }

    private static int removeProfession(CommandContext<CommandSourceStack> context, ResourceLocation id) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            if(taterzen.getProfessionIds().contains(id)) {
                taterzen.removeProfession(id);
                source.sendSuccess(successText("taterzens.command.profession.remove", id.toString()), false);
            } else
                context.getSource().sendFailure(errorText("taterzens.command.profession.error.404", id.toString()));
        });
    }

    private static int setProfession(CommandContext<CommandSourceStack> context, ResourceLocation id) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            if(PROFESSION_TYPES.containsKey(id)) {
                taterzen.addProfession(id);
                source.sendSuccess(successText("taterzens.command.profession.add", id.toString()), false);
            } else
                context.getSource().sendFailure(errorText("taterzens.command.profession.error.404", id.toString()));
        });
    }

    private static CompletableFuture<Suggestions> suggestRemovableProfessions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        Collection<ResourceLocation> professions = new HashSet<>();
        try {
            TaterzenNPC taterzen = ((ITaterzenEditor) ctx.getSource().getPlayerOrException()).getNpc();
            if(taterzen != null) {
                professions = taterzen.getProfessionIds();
            }
        } catch(CommandSyntaxException ignored) {
        }
        return SharedSuggestionProvider.suggest(professions.stream().map(ResourceLocation::toString), builder);
    }
}
