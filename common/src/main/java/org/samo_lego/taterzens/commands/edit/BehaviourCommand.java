package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.npc.NPCData;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.MODID;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.successText;
import static org.samo_lego.taterzens.util.TextUtil.translate;

public class BehaviourCommand {

    private static final SuggestionProvider<ServerCommandSource> HOSTILITY_TYPES;

    public static void registerNode(LiteralCommandNode<ServerCommandSource> editNode) {
        LiteralCommandNode<ServerCommandSource> behaviourNode = literal("behaviour")
                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.behaviour", config.perms.npcCommandPermissionLevel))
                .then(argument("behaviour", word())
                        .suggests(HOSTILITY_TYPES)
                        .executes(BehaviourCommand::setTaterzenBehaviour)
                )
                .build();

        editNode.addChild(behaviourNode);
    }

    private static int setTaterzenBehaviour(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            NPCData.Behaviour behaviour = NPCData.Behaviour.valueOf(StringArgumentType.getString(context, "behaviour"));
            taterzen.setBehaviour(behaviour);
            source.sendFeedback(successText("taterzens.command.behaviour.set", String.valueOf(behaviour)), false);
            if(behaviour != NPCData.Behaviour.PASSIVE && taterzen.isInvulnerable())
                source.sendFeedback(translate("taterzens.command.behaviour.suggest.invulnerable.false")
                                .formatted(Formatting.GOLD)
                                .formatted(Formatting.ITALIC)
                                .styled(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/data merge entity " + taterzen.getUuidAsString() + " {Invulnerable:0b}"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.disable_invulnerability")))
                                ),
                        false
                );
        });
    }

    static {
        HOSTILITY_TYPES = SuggestionProviders.register(
                new Identifier(MODID, "hostility_types"),
                (context, builder) ->
                        CommandSource.suggestMatching(Stream.of(NPCData.Behaviour.values()).map(Enum::name).collect(Collectors.toList()), builder)
        );
    }
}
