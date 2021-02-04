package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.EntitySummonArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.*;

import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.entity.EntityType.loadEntityWithPassengers;

public class NpcCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        // Ignore this for now, we will explain it next.
        dispatcher.register(CommandManager.literal("npc")
                .then(CommandManager.literal("spawn")
                    .then(CommandManager.argument("name", message())
                        .suggests((context, builder) -> CommandSource.suggestMatching(getOnlinePlayers(context), builder))
                        .executes(NpcCommand::spawnTaterzen)
                    )
                )
                .then(CommandManager.literal("select")
                        .then(CommandManager.argument("name", message())
                                .suggests((context, builder) -> CommandSource.suggestMatching(getOnlinePlayers(context), builder))
                                .executes(NpcCommand::spawnTaterzen)
                        )
                )
                .then(CommandManager.literal("changeType")
                        .then(CommandManager.argument("entityType", EntitySummonArgumentType.entitySummon())
                                .suggests((context, builder) -> CommandSource.suggestMatching(Collections.singleton("minecraft:player"), builder))
                                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                .executes(NpcCommand::changeType)
                        )
                )
        );
    }

    private static int changeType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            Identifier entityId = EntitySummonArgumentType.getEntitySummon(context, "entityType");
            CompoundTag tag = new CompoundTag();
            tag.putString("id", entityId.toString());
            Optional<Entity> newEntity = Optional.ofNullable(loadEntityWithPassengers(tag, context.getSource().getWorld(), (entityx) -> entityx));
            newEntity.ifPresent(entity -> taterzen.changeType(entity.getType()));
        }
        else
            context.getSource().sendError(new LiteralText("You have to select Taterzen first."));

        return 0;
    }

    private static Collection<String> getOnlinePlayers(CommandContext<ServerCommandSource> context) {
        Collection<String> names = new ArrayList<>();
        context.getSource().getMinecraftServer().getPlayerManager().getPlayerList().forEach(
                player -> names.add( player.getGameProfile().getName() )
        );

        return names;
    }

    private static int spawnTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = new TaterzenNPC(player, MessageArgumentType.getMessage(context, "name").asString());
        Taterzens.TATERZENS.add(taterzen);
        ((TaterzenEditor) player).selectNpc(taterzen);
        return 0;
    }
}
