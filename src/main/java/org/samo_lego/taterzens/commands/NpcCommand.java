package org.samo_lego.taterzens.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.entity.EntityType.loadEntityWithPassengers;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.lang;

public class NpcCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        // Ignore this for now, we will explain it next.
        dispatcher.register(literal("npc")
                .then(literal("create")
                    .then(argument("name", message())
                        .suggests((context, builder) -> CommandSource.suggestMatching(getOnlinePlayers(context), builder))
                        .executes(NpcCommand::spawnTaterzen)
                    )
                )
                .then(literal("select")
                        .then(literal("name")
                                .executes(NpcCommand::selectTaterzen)
                        )
                )
                .then(literal("remove")
                        .executes(NpcCommand::removeTaterzen)
                )
                .then(literal("edit")
                    .then(literal("command")
                            .redirect(dispatcher.getRoot(), context -> {
                                // Really ugly, but ... works :P
                                setCommand(context);
                                throw new SimpleCommandExceptionType(
                                        new LiteralText(lang.success.setCommandAction).formatted(Formatting.GREEN)
                                ).create();
                            })
                    )
                    .then(literal("type")
                            .then(argument("entity type", greedyString())
                                    .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                    .executes(NpcCommand::changeType)
                            )
                    )
                    .then(literal("skin")
                            .then(argument("player name", word())
                                    .executes(NpcCommand::setSkin)
                            )
                    )
                    .then(literal("equipment")
                        .executes(NpcCommand::setEquipment)
                    )
                )
        );
    }

    private static int setEquipment(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            if(taterzen.isEquipmentEditor(player)) {
                context.getSource().sendFeedback(
                        new LiteralText(lang.success.editorExit).formatted(Formatting.LIGHT_PURPLE),
                        false
                );

                taterzen.setEquipmentEditor(null);
            }
            else {
                context.getSource().sendFeedback(
                        new LiteralText(String.format(lang.success.equipmentEditorEnter, taterzen.getName().asString())).formatted(Formatting.GOLD),
                        false
                );
                context.getSource().sendFeedback(
                        new LiteralText(lang.success.equipmentEditorDesc).formatted(Formatting.YELLOW),
                        false
                );

                taterzen.setEquipmentEditor(player);
            }

        }
        else
            context.getSource().sendError(
                    new LiteralText(lang.error.selectTaterzen).formatted(Formatting.RED)
            );

        return 0;
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
            context.getSource().sendError(
                    new LiteralText(lang.error.selectTaterzen).formatted(Formatting.RED)
            );
        return 0;
    }

    private static void setCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TaterzenNPC taterzen = ((TaterzenEditor) context.getSource().getPlayer()).getNpc();
        // Extremely :concern:
        // I know itd
        String command = context.getInput().substring(18); // 18 being the length of `/npc edit command `

        taterzen.setCommand(command);
    }

    private static int removeTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TaterzenNPC taterzen = ((TaterzenEditor) context.getSource().getPlayer()).getNpc();
        if(taterzen != null) {
            taterzen.kill();
            context.getSource().sendFeedback(
                    new LiteralText(lang.success.killedTaterzen).formatted(Formatting.RED).append(taterzen.getName()).formatted(Formatting.YELLOW),
                    false
            );
        }
        else
            context.getSource().sendError(
                    new LiteralText(lang.error.selectTaterzen).formatted(Formatting.RED)
            );
        ((TaterzenEditor) context.getSource().getPlayer()).selectNpc(null);
        return 0;
    }

    private static int selectTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        Box box = player.getBoundingBox().offset(player.getRotationVector());
        player.getEntityWorld().getEntityCollisions(player, box, entity -> {
            if(entity instanceof TaterzenNPC) {
                ((TaterzenEditor) player).selectNpc((TaterzenNPC) entity);
                context.getSource().sendFeedback(
                        new LiteralText(lang.success.selectedTaterzen).formatted(Formatting.GREEN).append(entity.getName()).formatted(Formatting.YELLOW),
                        false
                );
                return true;
            }
            return false;
        });

        //TaterzenNPC taterzen = (TaterzenNPC) EntityArgumentType.getEntity(context, "name");
        //Scontext.getArgument("name", EntitySelector.class).getEntity(context.getSource());
        //((TaterzenEditor) player).selectNpc(taterzen);

        return 0;
    }

    private static int changeType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
            if(taterzen != null) {
                String entityId = StringArgumentType.getString(context, "entity type");
                if(entityId.equals("player") || entityId.equals("minecraft:player")) {
                    //Minecraft has built-in protection against creating players :(
                    taterzen.changeType(player);
                }
                else {
                    CompoundTag tag = new CompoundTag();

                    tag.putString("id", entityId);
                    Optional<Entity> optionalEntity = Optional.ofNullable(loadEntityWithPassengers(tag, context.getSource().getWorld(), (entity) -> entity));
                    optionalEntity.ifPresent(taterzen::changeType);

                }

                context.getSource().sendFeedback(
                        new LiteralText(String.format(lang.success.changedType, entityId)).formatted(Formatting.GREEN),
                        false
                );
            }
            else
                context.getSource().sendError(
                        new LiteralText(lang.error.selectTaterzen).formatted(Formatting.RED)
                );
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
            String taterzenName = MessageArgumentType.getMessage(context, "name").asString();
            TaterzenNPC taterzen = new TaterzenNPC(player, taterzenName);
            ((TaterzenEditor) player).selectNpc(taterzen);
            context.getSource().sendFeedback(
                    new LiteralText(String.format(lang.success.spawnedTaterzen, taterzenName)).formatted(Formatting.GREEN),
                    false
            );
        } catch (ClassCastException | NoSuchElementException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
