package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.npc.NPCData;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.MOD_ID;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.successText;
import static org.samo_lego.taterzens.util.TextUtil.translate;

public class BehaviourCommand {

    private static final SuggestionProvider<CommandSourceStack> HOSTILITY_TYPES;

    public static void registerNode(LiteralCommandNode<CommandSourceStack> editNode) {
        LiteralCommandNode<CommandSourceStack> behaviourNode = literal("behaviour")
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.behaviour", config.perms.npcCommandPermissionLevel))
                .then(argument("behaviour", word())
                        .suggests(HOSTILITY_TYPES)
                        .executes(BehaviourCommand::setTaterzenBehaviour)
                )
                .build();

        editNode.addChild(behaviourNode);
    }

    private static int setTaterzenBehaviour(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            NPCData.Behaviour behaviour = NPCData.Behaviour.valueOf(StringArgumentType.getString(context, "behaviour"));
            taterzen.setBehaviour(behaviour);
            source.sendSuccess(successText("taterzens.command.behaviour.set", String.valueOf(behaviour)), false);
            if(behaviour != NPCData.Behaviour.PASSIVE && taterzen.isInvulnerable())
                source.sendSuccess(translate("taterzens.command.behaviour.suggest.invulnerable.false")
                                .withStyle(ChatFormatting.GOLD)
                                .withStyle(ChatFormatting.ITALIC)
                                .withStyle(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/data merge entity " + taterzen.getStringUUID() + " {Invulnerable:0b}"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.disable_invulnerability")))
                                ),
                        false
                );
        });
    }

    static {
        HOSTILITY_TYPES = SuggestionProviders.register(
                new ResourceLocation(MOD_ID, "hostility_types"),
                (context, builder) ->
                        SharedSuggestionProvider.suggest(Stream.of(NPCData.Behaviour.values()).map(Enum::name).collect(Collectors.toList()), builder)
        );
    }
}
