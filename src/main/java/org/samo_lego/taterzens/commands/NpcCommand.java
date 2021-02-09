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
import org.samo_lego.taterzens.npc.NPCData;
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
import static org.samo_lego.taterzens.util.TextUtil.*;

public class NpcCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        // Ignore this for now, we will explain it next.
        dispatcher.register(literal("npc")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
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
                                String cmd = setCommand(context);
                                throw new SimpleCommandExceptionType(
                                        joinString(lang.success.setCommandAction, Formatting.GOLD, "/" + cmd, Formatting.GRAY)
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
                    .then(literal("look")
                        .executes(context -> changeMovement(context, NPCData.Movement.LOOK))
                    )
                    .then(literal("movement")
                            .then(literal("free")
                                    .executes(context -> changeMovement(context, NPCData.Movement.FREE))
                            )
                            .then(literal("path")
                                    .executes(context -> changeMovement(context, NPCData.Movement.LOOK))
                            )
                            .then(literal("none")
                                    .executes(context -> changeMovement(context, NPCData.Movement.NONE))
                            )
                    )
                )
        );
    }

    private static int changeMovement(CommandContext<ServerCommandSource> context, NPCData.Movement movement) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            taterzen.setMovement(movement);
            context.getSource().sendFeedback(
                    successText(lang.success.changedMovementType, new LiteralText(movement.toString())),
                    false
            );
        }
        else
            context.getSource().sendError(
                    new LiteralText(lang.error.selectTaterzen).formatted(Formatting.RED)
            );
        return 0;
    }

    private static int setEquipment(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            if(taterzen.isEquipmentEditor(player)) {
                taterzen.setEquipmentEditor(null);
                context.getSource().sendFeedback(
                        new LiteralText(lang.success.editorExit).formatted(Formatting.LIGHT_PURPLE),
                        false
                );

                taterzen.setEquipmentEditor(null);
            }
            else {
                context.getSource().sendFeedback(
                        joinText(lang.success.equipmentEditorEnter, Formatting.LIGHT_PURPLE, taterzen.getName(), Formatting.AQUA).formatted(Formatting.BOLD),
                        false
                );
                context.getSource().sendFeedback(
                        new LiteralText(lang.success.equipmentEditorDescLine1).formatted(Formatting.YELLOW).append("\n")
                            .append(new LiteralText(lang.success.equipmentEditorDescLine2).formatted(Formatting.GOLD)).append("\n")
                            .append(lang.success.equipmentEditorDescLine3).formatted(Formatting.YELLOW).append("\n")
                            .append(new LiteralText(lang.success.equipmentEditorDescLine4).formatted(Formatting.RED)),
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
            String skinPlayerName = StringArgumentType.getString(context, "player name");
            GameProfile skinProfile = new GameProfile(null, skinPlayerName);
            skinProfile = SkullBlockEntity.loadProperties(skinProfile);
            taterzen.applySkin(skinProfile, true);
            context.getSource().sendFeedback(
                    successText(lang.success.taterzenSkinChange, new LiteralText(skinPlayerName)),
                    false
            );
        }
        else
            context.getSource().sendError(
                    new LiteralText(lang.error.selectTaterzen).formatted(Formatting.RED)
            );
        return 0;
    }

    private static String setCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TaterzenNPC taterzen = ((TaterzenEditor) context.getSource().getPlayer()).getNpc();
        // Extremely :concern:
        // I know it
        String command = context.getInput().substring(18); // 18 being the length of `/npc edit command `
        taterzen.setCommand(command);

        return command;
    }

    private static int removeTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TaterzenNPC taterzen = ((TaterzenEditor) context.getSource().getPlayer()).getNpc();
        if(taterzen != null) {
            taterzen.kill();
            context.getSource().sendFeedback(
                    successText(lang.success.killedTaterzen, taterzen.getName()),
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
                        successText(lang.success.selectedTaterzen, entity.getName()),
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
                        joinString(lang.success.changedEntityType, Formatting.GREEN,  entityId, Formatting.YELLOW),
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
                    successText(lang.success.spawnedTaterzen, taterzen.getName()),
                    false
            );
        } catch (ClassCastException | NoSuchElementException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
