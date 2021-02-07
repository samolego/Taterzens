package org.samo_lego.taterzens.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.block.entity.SkullBlockEntity;
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
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.entity.EntityType.loadEntityWithPassengers;

public class NpcCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        // Ignore this for now, we will explain it next.
        dispatcher.register(CommandManager.literal("npc")
                .then(CommandManager.literal("create")
                    .then(CommandManager.argument("name", message())
                        .suggests((context, builder) -> CommandSource.suggestMatching(getOnlinePlayers(context), builder))
                        .executes(NpcCommand::spawnTaterzen)
                    )
                )
                .then(CommandManager.literal("select")
                        .then(CommandManager.argument("name", EntityArgumentType.entity())
                                .executes(NpcCommand::selectTaterzen)
                        )
                )
                .then(CommandManager.literal("remove")
                        .executes(NpcCommand::removeTaterzen)
                )
                .then(CommandManager.literal("edit")
                    .then(CommandManager.literal("setCommand")
                            .redirect(dispatcher.getRoot(), context -> {
                                // Really ugly, but ... works :P
                                setCommand(context);
                                throw new SimpleCommandExceptionType(
                                        new LiteralText("Success!")
                                                .formatted(Formatting.GREEN)
                                ).create();
                            })
                    )
                    .then(CommandManager.literal("changeType")
                            .then(CommandManager.argument("entityType", EntitySummonArgumentType.entitySummon())
                                    .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                    .executes(NpcCommand::changeType)
                            )
                    )
                    .then(CommandManager.literal("setSkin")
                            .then(CommandManager.argument("player name", word())
                                    .executes(NpcCommand::setSkin)
                            )
                    )
                )
        );
    }

    private static int setSkin(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            GameProfile skinProfile = new GameProfile(null, StringArgumentType.getString(context, "player name"));
            skinProfile = SkullBlockEntity.loadProperties(skinProfile);
            System.out.println(skinProfile);
            taterzen.applySkin(skinProfile, true);
        }
        else
            context.getSource().sendError(new LiteralText("You have to select Taterzen first."));
        return 0;
    }

    private static void setCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TaterzenNPC taterzen = ((TaterzenEditor) context.getSource().getPlayer()).getNpc();
        String command = context.getInput().substring(21);

        taterzen.setCommand(command);
    }

    private static int removeTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ((TaterzenEditor) context.getSource().getPlayer()).getNpc().kill();
        ((TaterzenEditor) context.getSource().getPlayer()).selectNpc(null);
        return 0;
    }

    private static int selectTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = (TaterzenNPC) EntityArgumentType.getEntity(context, "name");
        ((TaterzenEditor) player).selectNpc(taterzen);

        return 0;
    }

    private static int changeType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
            if(taterzen != null) {
                Identifier entityId = EntitySummonArgumentType.getEntitySummon(context, "entityType");
                CompoundTag tag = new CompoundTag();
                tag.putString("id", entityId.toString());
                Optional<Entity> optionalEntity = Optional.ofNullable(loadEntityWithPassengers(tag, context.getSource().getWorld(), (entity) -> entity));
                optionalEntity.ifPresent(taterzen::changeType);
            }
            else
                context.getSource().sendError(new LiteralText("You have to select Taterzen first."));
        } catch (Error e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static Collection<String> getOnlinePlayers(CommandContext<ServerCommandSource> context) {
        Collection<String> names = new ArrayList<>();
        context.getSource().getMinecraftServer().getPlayerManager().getPlayerList().forEach(
                player -> names.add( player.getGameProfile().getName() )
        );

        return names;
    }

    /*private static Collection<String> getEntites(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        CompletableFuture<Suggestions> names = SuggestionProviders.SUMMONABLE_ENTITIES.getSuggestions(context, builder);
        try {
            Suggestions suggestions = names.get();
            suggestions.getList().add(new Suggestion())
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        context.getSource().getMinecraftServer().getPlayerManager().getPlayerList().forEach(
                player -> names.add( player.getGameProfile().getName() )
        );

        return names;
    }*/

    private static int spawnTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            TaterzenNPC taterzen = new TaterzenNPC(player, MessageArgumentType.getMessage(context, "name").asString());
            ((TaterzenEditor) player).selectNpc(taterzen);
        } catch (ClassCastException | NoSuchElementException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
