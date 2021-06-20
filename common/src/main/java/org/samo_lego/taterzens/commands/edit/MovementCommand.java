package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.npc.NPCData;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.MODID;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.errorText;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class MovementCommand {

    private static final SuggestionProvider<ServerCommandSource> MOVEMENT_TYPES;
    private static final SuggestionProvider<ServerCommandSource> FOLLOW_TYPES;

    public static void registerNode(LiteralCommandNode<ServerCommandSource> editNode) {
        LiteralCommandNode<ServerCommandSource> movementNode = literal("movement")
                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.movement", config.perms.npcCommandPermissionLevel))
                .then(literal("FOLLOW")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.movement.follow", config.perms.npcCommandPermissionLevel))
                        .then(argument("follow type", word())
                                .suggests(FOLLOW_TYPES)
                                .executes(ctx -> setFollowType(ctx, NPCData.FollowTypes.valueOf(StringArgumentType.getString(ctx, "follow type"))))
                                .then(argument("uuid", EntityArgumentType.entity())
                                        .executes(ctx -> setFollowType(ctx, NPCData.FollowTypes.valueOf(StringArgumentType.getString(ctx, "follow type"))))
                                )
                        )
                )
                .then(argument("movement type", word())
                        .suggests(MOVEMENT_TYPES)
                        .executes(context -> changeMovement(context, StringArgumentType.getString(context, "movement type")))
                )
                .build();


        LiteralCommandNode<ServerCommandSource> lookNode = literal("look")
                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.movement", config.perms.npcCommandPermissionLevel))
                .executes(context -> changeMovement(context, "FORCED_LOOK"))
                .build();

        editNode.addChild(movementNode);
        editNode.addChild(lookNode);
    }

    private static int changeMovement(CommandContext<ServerCommandSource> context, String movement) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            taterzen.setMovement(NPCData.Movement.valueOf(movement));
            source.sendFeedback(
                    successText("taterzens.command.movement.set", movement),
                    false
            );
        });
    }

    private static int setFollowType(CommandContext<ServerCommandSource> context, NPCData.FollowTypes followType) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            taterzen.setFollowType(followType);
            if(followType == NPCData.FollowTypes.UUID) {
                try {
                    UUID uuid = EntityArgumentType.getEntity(context, "uuid").getUuid();
                    taterzen.setFollowUuid(uuid);
                } catch(CommandSyntaxException ignored) {
                    source.sendError(errorText("taterzens.command.movement.follow.error.uuid", followType.toString()));
                }
            }

            source.sendFeedback(successText("taterzens.command.movement.follow.set", followType.toString()), false);
        });
    }

    static {
        MOVEMENT_TYPES = SuggestionProviders.register(
                new Identifier(MODID, "movement_types"),
                (context, builder) ->
                        CommandSource.suggestMatching(Stream.of(NPCData.Movement.values()).map(Enum::name).collect(Collectors.toList()), builder)
        );

        FOLLOW_TYPES = SuggestionProviders.register(
                new Identifier(MODID, "follow_types"),
                (context, builder) ->
                        CommandSource.suggestMatching(Stream.of(NPCData.FollowTypes.values()).map(Enum::name).collect(Collectors.toList()), builder)
        );
    }
}
