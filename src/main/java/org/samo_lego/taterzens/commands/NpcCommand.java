package org.samo_lego.taterzens.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.*;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.samo_lego.taterzens.api.TaterzensAPI;
import org.samo_lego.taterzens.compatibility.DisguiseLibCompatibility;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.NPCData;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.command.suggestion.SuggestionProviders.SUMMONABLE_ENTITIES;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.*;
import static org.samo_lego.taterzens.api.TaterzensAPI.getPresets;
import static org.samo_lego.taterzens.api.TaterzensAPI.noSelectedTaterzenError;
import static org.samo_lego.taterzens.compatibility.PermissionHelper.checkPermission;
import static org.samo_lego.taterzens.mixin.accessors.PlayerEntityAccessor.getPLAYER_MODEL_PARTS;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class NpcCommand {

    private static final SuggestionProvider<ServerCommandSource> MOVEMENT_TYPES;
    private static final SuggestionProvider<ServerCommandSource> HOSTILITY_TYPES;


    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("npc")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4) || LUCKPERMS_ENABLED)
                .then(literal("create")
                        .then(argument("name", message())
                                .suggests((context, builder) -> CommandSource.suggestMatching(getOnlinePlayers(context), builder))
                                .executes(NpcCommand::spawnTaterzen)
                        )
                )
                .then(literal("select")
                        .then(argument("id", IntegerArgumentType.integer(1)).executes(NpcCommand::selectTaterzenById))
                        .executes(NpcCommand::selectTaterzen)
                )
                .then(literal("deselect").executes(NpcCommand::deselectTaterzen))
                .then(literal("list").executes(NpcCommand::listTaterzens))
                .then(literal("remove").executes(NpcCommand::removeTaterzen))
                .then(literal("preset")
                        .then(literal("save")
                                .then(argument("preset name", word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(getPresets(), builder))
                                        .executes(NpcCommand::saveTaterzenToPreset)
                                )
                        )
                        .then(literal("load")
                                .then(argument("preset name", word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(getPresets(), builder))
                                        .executes(NpcCommand::loadTaterzenFromPreset)
                                )
                        )
                )
                .then(literal("tp")
                        .then(argument("destination", EntityArgumentType.entity())
                                .executes(context -> teleportTaterzen(context, EntityArgumentType.getEntity(context, "destination").getPos()))
                        )
                        .then(argument("position", Vec3ArgumentType.vec3())
                                .executes(context -> teleportTaterzen(context, Vec3ArgumentType.getPosArgument(context, "position").toAbsolutePos(context.getSource())))
                        )
                )
                .then(literal("edit")
                        .then(literal("name").then(argument("new name", message()).executes(NpcCommand::renameTaterzen)))
                        .then(literal("commands")
                                .then(literal("setPermissionLevel")
                                        .then(argument("level", IntegerArgumentType.integer(0, 4))
                                                .executes(NpcCommand::setPermissionLevel)
                                        )
                                )
                                .then(literal("remove").then(argument("command id", IntegerArgumentType.integer(0)).executes(NpcCommand::removeCommand)))
                                .then(literal("add")
                                        .redirect(dispatcher.getRoot(), context -> {
                                            // Really ugly, but ... works :P
                                            String cmd = addCommand(context);
                                            throw new SimpleCommandExceptionType(
                                                    cmd == null ?
                                                            noSelectedTaterzenError() :
                                                            joinString(lang.success.setCommandAction, Formatting.GOLD, "/" + cmd, Formatting.GRAY)
                                            ).create();
                                        })
                                )
                                .then(literal("clear").executes(NpcCommand::clearCommands))
                                .then(literal("list").executes(NpcCommand::listTaterzenCommands))
                        )
                        .then(literal("behaviour")
                            .then(argument("behaviour", word())
                                    .suggests(HOSTILITY_TYPES)
                                    .executes(NpcCommand::setTaterzenBehaviour)
                            )
                        )
                        .then(literal("invulnerable")
                                .then(argument("invulnerable", BoolArgumentType.bool()).executes(NpcCommand::setInvulnerable))
                        )
                        .then(literal("type")
                                .then(argument("entity type", EntitySummonArgumentType.entitySummon())
                                        .suggests(SUMMONABLE_ENTITIES)
                                        .executes(NpcCommand::changeType)
                                        .then(argument("nbt", NbtCompoundTagArgumentType.nbtCompound())
                                                .executes(NpcCommand::changeType)
                                        )
                                )
                                .then(literal("minecraft:player").executes(NpcCommand::resetType))
                                .then(literal("player").executes(NpcCommand::resetType))
                                .then(literal("reset").executes(NpcCommand::resetType))
                        )
                        .then(literal("path").executes(NpcCommand::editTaterzenPath)
                            .then(literal("clear").executes(NpcCommand::clearTaterzenPath))
                        )
                        .then(literal("messages").executes(NpcCommand::editTaterzenMessages)
                                .then(literal("clear").executes(NpcCommand::clearTaterzenMessages))
                                .then(literal("list").executes(NpcCommand::listTaterzenMessages))
                                .then(argument("message id", IntegerArgumentType.integer(0))
                                        .then(literal("delete").executes(NpcCommand::deleteTaterzenMessage))
                                        .then(literal("setDelay")
                                                .then(argument("delay", IntegerArgumentType.integer())
                                                        .executes(NpcCommand::editMessageDelay))
                                        )
                                        .executes(NpcCommand::editMessage)
                                )
                        )
                        .then(literal("skin")
                                .executes(NpcCommand::copySkinLayers)
                                .then(argument("player name", word()).executes(NpcCommand::setSkin))
                        )
                        .then(literal("equipment")
                                .then(literal("allowEquipmentDrops")
                                        .then(argument("drop", BoolArgumentType.bool()).executes(NpcCommand::setEquipmentDrops))
                                )
                                .executes(NpcCommand::setEquipment)
                        )
                        .then(literal("look").executes(context -> changeMovement(context, "FORCED_LOOK")))
                        .then(literal("movement")
                                .then(argument("movement type", word())
                                        .suggests(MOVEMENT_TYPES)
                                        .executes(context -> changeMovement(context, StringArgumentType.getString(context, "movement type")))
                                )
                        )
                )
        );
    }

    private static int setEquipmentDrops(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_equipment_equipmentDrops)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            boolean drop = BoolArgumentType.getBool(context, "drop");
            taterzen.allowEquipmentDrops(drop);
            player.sendMessage(successText(lang.success.equipmentDropStatus, new LiteralText(String.valueOf(drop))), false);
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int setInvulnerable(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_tags_invulnerability)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            boolean invulnerable = BoolArgumentType.getBool(context, "invulnerable");
            taterzen.setInvulnerable(invulnerable);
            player.sendMessage(successText(lang.success.invulnerableStatus, new LiteralText(String.valueOf(invulnerable))), false);
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int setTaterzenBehaviour(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_behaviour)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            NPCData.Behaviour behaviour = NPCData.Behaviour.valueOf(StringArgumentType.getString(context, "behaviour"));
            taterzen.setBehaviour(behaviour);
            player.sendMessage(successText(lang.success.behaviour, new LiteralText(String.valueOf(behaviour))), false);
            if(behaviour != NPCData.Behaviour.PASSIVE && taterzen.isInvulnerable())
                player.sendMessage(new LiteralText(lang.success.behaviourSuggestion)
                        .formatted(Formatting.GOLD)
                        .formatted(Formatting.ITALIC)
                        .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc edit invulnerable false"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Disable invulnerability")))
                        ),
                        false
                );
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int removeCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_commands_remove)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            int selected = IntegerArgumentType.getInteger(context, "command id") - 1;
            if(selected >= taterzen.getCommands().size()) {
                player.sendMessage(
                        errorText(lang.error.noCommandFound, new LiteralText(String.valueOf(selected))),
                        false
                );
            } else {
                player.sendMessage(successText(lang.success.commandRemoved, new LiteralText(String.valueOf(taterzen.getCommands().get(selected)))), false);
                taterzen.removeCommand(selected);
            }
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int clearCommands(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_commands_clear)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            player.sendMessage(successText(lang.success.commandsCleared, taterzen.getName()), false);
            taterzen.clearCommands();

        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int listTaterzenCommands(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_commands_clear)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            ArrayList<String> messages = taterzen.getCommands();

            MutableText response = joinText(lang.taterzenCommands, Formatting.AQUA, taterzen.getCustomName(), Formatting.YELLOW);
            if(!messages.isEmpty()) {
                AtomicInteger i = new AtomicInteger();

                messages.forEach(cmd -> {
                    int index = i.get() + 1;
                    response.append(
                            new LiteralText("\n" + index + "-> ")
                                    .formatted(index % 2 == 0 ? Formatting.YELLOW : Formatting.GOLD)
                                    .append(cmd)
                                    .append("   ")
                                    .append(
                                            new LiteralText("X")
                                                    .formatted(Formatting.RED)
                                                    .formatted(Formatting.BOLD)
                                                    .styled(style -> style
                                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Delete " + index)))
                                                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc edit commands remove " + index))
                                                    )
                                    )
                    );
                    i.incrementAndGet();
                });
            }

            player.sendMessage(response, false);
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int setPermissionLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_commands_setPermissionLevel)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            int newPermLevel = IntegerArgumentType.getInteger(context, "level");
            player.sendMessage(successText(lang.success.updatedPermissionLevel, new LiteralText(String.valueOf(newPermLevel))), false);
            taterzen.setPermissionLevel(newPermLevel);

        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int deselectTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_deselect)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ((TaterzenEditor) context.getSource().getPlayer()).selectNpc(null);
        context.getSource().sendFeedback(new LiteralText(lang.success.deselectedTaterzen).formatted(Formatting.GREEN), false);
        return 0;
    }

    private static int deleteTaterzenMessage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_messages_delete)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            int selected = IntegerArgumentType.getInteger(context, "message id") - 1;
            if(selected >= taterzen.getMessages().size()) {
                player.sendMessage(
                        errorText(lang.error.noMessageFound, new LiteralText(String.valueOf(selected))),
                        false
                );
            } else {
                player.sendMessage(successText(lang.success.messageDeleted, taterzen.getMessages().get(selected).getFirst()), false);
                taterzen.removeMessage(selected);
            }
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int editMessageDelay(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_messages_delay)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            int selected = IntegerArgumentType.getInteger(context, "message id") - 1;
            if(selected >= taterzen.getMessages().size()) {
                player.sendMessage(
                        errorText(lang.error.noMessageFound, new LiteralText(String.valueOf(selected))),
                        false
                );
            } else {
                int delay = IntegerArgumentType.getInteger(context, "delay");
                taterzen.setMessageDelay(selected, delay);
                player.sendMessage(successText(lang.success.messageDelaySet, new LiteralText(String.valueOf(delay))), false);
            }
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int editMessage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_messages_delete)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            if(((TaterzenEditor) player).inMsgEditMode()) {
                int selected = IntegerArgumentType.getInteger(context, "message id") - 1;
                if(selected >= taterzen.getMessages().size()) {
                    player.sendMessage(
                            successText(lang.error.noMessageFound, new LiteralText(String.valueOf(selected))),
                            false
                    );
                } else {
                    ((TaterzenEditor) player).setEditingMessageIndex(selected);
                    player.sendMessage(successText(lang.editMessageMode, taterzen.getMessages().get(selected).getFirst()), false);
                }
            } else {
                player.sendMessage(new LiteralText(lang.error.enterMessageEditorMode)
                        .formatted(Formatting.RED)
                        .styled(style -> style
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(lang.enterMessageEditor)))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc edit messages"))
                        ),
                        false
                );
            }
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int listTaterzenMessages(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_messages_list)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            ((TaterzenEditor) player).setMsgEditMode(true);
            ArrayList<Pair<Text, Integer>> messages = taterzen.getMessages();

            MutableText response = joinText(lang.taterzenMessages, Formatting.AQUA, taterzen.getCustomName(), Formatting.YELLOW);
            AtomicInteger i = new AtomicInteger();

            messages.forEach(pair -> {
                int index = i.get() + 1;
                response.append(
                        new LiteralText("\n" + index + "-> ")
                                .formatted(index % 2 == 0 ? Formatting.YELLOW : Formatting.GOLD)
                                .append(pair.getFirst())
                                .styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, pair.getFirst().getString())))
                                .styled(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit messages " + index))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Edit message ")
                                                .append(String.valueOf(index))
                                        ))
                                )
                        .append("   ")
                        .append(
                                new LiteralText("X")
                                    .formatted(Formatting.RED)
                                    .formatted(Formatting.BOLD)
                                    .styled(style -> style
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Delete " + index)))
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc edit messages " + index + " delete"))
                                    )
                        )
                );
                i.incrementAndGet();
            });
            player.sendMessage(response, false);
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int clearTaterzenMessages(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_messages_clear)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            taterzen.clearMessages();
            player.sendMessage(successText(lang.success.messagesCleared, taterzen.getName()), false);
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int editTaterzenMessages(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_messages_edit)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            if(((TaterzenEditor) player).inMsgEditMode()) {
                // Exiting the message edit mode
                ((TaterzenEditor) player).setMsgEditMode(false);
                ((TaterzenEditor) player).setEditingMessageIndex(-1);
                context.getSource().sendFeedback(
                        new LiteralText(lang.success.editorExit).formatted(Formatting.LIGHT_PURPLE),
                        false
                );
            } else {
                // Entering the edit mode
                ((TaterzenEditor) player).setMsgEditMode(true);
                context.getSource().sendFeedback(
                        joinText(lang.success.msgEditorEnter, Formatting.LIGHT_PURPLE, taterzen.getCustomName(), Formatting.AQUA)
                                .formatted(Formatting.BOLD)
                                .styled(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit messages"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Exit").formatted(Formatting.RED)))
                                ),
                        false
                );
                context.getSource().sendFeedback(
                        successText(lang.success.msgEditorDescLine1, taterzen.getCustomName())
                                .append("\n")
                                .append(new LiteralText(lang.success.msgEditorDescLine2))
                                .formatted(Formatting.GREEN),
                        false
                );
            }
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int clearTaterzenPath(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_path_clear)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            World world = player.getEntityWorld();
            taterzen.getPathTargets().forEach(blockPos -> player.networkHandler.sendPacket(
                    new BlockUpdateS2CPacket(blockPos, world.getBlockState(blockPos))
            ));
            taterzen.clearPathTargets();
            context.getSource().sendFeedback(
                    successText(lang.success.clearPath, taterzen.getCustomName()),
                    false
            );
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int editTaterzenPath(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_path)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            if(((TaterzenEditor) player).inPathEditMode()) {
                ((TaterzenEditor) player).setPathEditMode(false);
                context.getSource().sendFeedback(
                        new LiteralText(lang.success.editorExit).formatted(Formatting.LIGHT_PURPLE),
                        false
                );

            } else {

                context.getSource().sendFeedback(
                        joinText(lang.success.pathEditorEnter, Formatting.LIGHT_PURPLE, taterzen.getCustomName(), Formatting.AQUA)
                                .formatted(Formatting.BOLD)
                                .styled(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit path"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Exit").formatted(Formatting.RED)))
                                ),
                        false
                );
                context.getSource().sendFeedback(
                        new LiteralText(lang.success.pathEditorDescLine1).append("\n").formatted(Formatting.BLUE)
                                .append(new LiteralText(lang.success.pathEditorDescLine2).formatted(Formatting.RED)),
                        false
                );

                ((TaterzenEditor) player).setPathEditMode(true);
            }

        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int loadTaterzenFromPreset(CommandContext<ServerCommandSource> context) {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_preset_load)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        String filename = StringArgumentType.getString(context, "preset name") + ".json";
        File preset = new File(getPresetDir() + "/" + filename);

        if(preset.exists()) {
            TaterzenNPC taterzenNPC = TaterzensAPI.loadTaterzenFromPreset(preset, context.getSource().getWorld());
            assert taterzenNPC != null;
            Vec3d pos = context.getSource().getPosition();
            Vec2f rotation = context.getSource().getRotation();
            taterzenNPC.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), rotation.x, rotation.y);

            context.getSource().getWorld().spawnEntity(taterzenNPC);

            try {
                ServerPlayerEntity player = context.getSource().getPlayer();
                ((TaterzenEditor) player).selectNpc(taterzenNPC);
            } catch(CommandSyntaxException ignored) {
            }

            context.getSource().sendFeedback(
                    successText(lang.success.importedTaterzenPreset, new LiteralText(filename)),
                    false
            );
        } else {
            context.getSource().sendError(
                    errorText(lang.error.noPresetFound, new LiteralText(filename))
            );
        }
        return 0;
    }

    private static int saveTaterzenToPreset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_preset_save)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            String filename = StringArgumentType.getString(context, "preset name") + ".json";
            File preset = new File(getPresetDir() + "/" + filename);
            TaterzensAPI.saveTaterzenToPreset(taterzen, preset);

            context.getSource().sendFeedback(
                    successText(lang.success.exportedTaterzen, new LiteralText(filename)),
                    false
            );
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int listTaterzens(CommandContext<ServerCommandSource> context) {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_list)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        MutableText response = new LiteralText(lang.availableTaterzens).formatted(Formatting.AQUA);
        for(int i = 0; i < TATERZEN_NPCS.size(); ++i) {
            int index = i + 1;
            Text name = ((TaterzenNPC) TATERZEN_NPCS.toArray()[i]).getCustomName();
            response.append(
                    new LiteralText("\n" + index + "-> ")
                            .append(name)
                            .formatted(i % 2 == 0 ? Formatting.YELLOW : Formatting.GOLD)
                            .styled(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc select " + index))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Select ").append(name)))
                            )
            );
        }

        context.getSource().sendFeedback(response, false);
        return 0;
    }

    private static int selectTaterzenById(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_select_id)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        int id = IntegerArgumentType.getInteger(context, "id");
        if(id > TATERZEN_NPCS.size()) {
            context.getSource().sendError(
                    errorText(lang.error.noTaterzenFound, new LiteralText(String.valueOf(id)))
            );
        } else {
            TaterzenNPC taterzen = (TaterzenNPC) TATERZEN_NPCS.toArray()[id - 1];
            ((TaterzenEditor) player).selectNpc(taterzen);
            context.getSource().sendFeedback(
                    successText(lang.success.selectedTaterzen, taterzen.getCustomName()),
                    false
            );
        }
        return 0;
    }

    private static int renameTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_name)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            Text newName = MessageArgumentType.getMessage(context, "new name");
            taterzen.setCustomName(newName);
            context.getSource().sendFeedback(
                    successText(lang.success.renameTaterzen, newName),
                    false
            );
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int teleportTaterzen(CommandContext<ServerCommandSource> context, Vec3d destination) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_tp)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            taterzen.teleport(destination.getX(), destination.getY(), destination.getZ());
        } else
            context.getSource().sendError(noSelectedTaterzenError());
        return 0;
    }

    private static int changeMovement(CommandContext<ServerCommandSource> context, String movement) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_movement)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            taterzen.setMovement(NPCData.Movement.valueOf(movement));
            context.getSource().sendFeedback(
                    successText(lang.success.changedMovementType, new LiteralText(movement)),
                    false
            );
        } else
            context.getSource().sendError(noSelectedTaterzenError());
        return 0;
    }

    private static int setEquipment(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_equipment)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

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
            } else {
                context.getSource().sendFeedback(
                        joinText(lang.success.equipmentEditorEnter, Formatting.LIGHT_PURPLE, taterzen.getCustomName(), Formatting.AQUA)
                                .formatted(Formatting.BOLD)
                                .styled(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit equipment"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Exit").formatted(Formatting.RED)))
                                ),
                        false
                );
                context.getSource().sendFeedback(
                        new LiteralText(lang.success.equipmentEditorDescLine1).append("\n")
                                .append(lang.success.equipmentEditorDescLine2).append("\n")
                                .append(lang.success.equipmentEditorDescLine3).formatted(Formatting.YELLOW).append("\n")
                                .append(new LiteralText(lang.success.equipmentEditorDescLine4).formatted(Formatting.RED)),
                        false
                );

                taterzen.setEquipmentEditor(player);
            }

        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int setSkin(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_skin)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();

        // Shameless self-promotion
        if(config.fabricTailorAdvert) {
            if(FABRICTAILOR_LOADED) {
                player.sendMessage(new LiteralText(lang.skinCommandUsage)
                        .formatted(Formatting.GOLD)
                        .styled(style ->
                            style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skin set"))
                        ),
                    false
                );
            } else {
                player.sendMessage(new LiteralText(lang.fabricTailorAdvert)
                                .formatted(Formatting.ITALIC)
                                .formatted(Formatting.GOLD)
                                .styled(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/FabricTailor"))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Install FabricTailor")))
                                ),
                        false
                );
            }

        }

        if(taterzen != null) {
            String skinPlayerName = StringArgumentType.getString(context, "player name");
            GameProfile skinProfile = new GameProfile(null, skinPlayerName);
            skinProfile = SkullBlockEntity.loadProperties(skinProfile);
            taterzen.applySkin(skinProfile);
            context.getSource().sendFeedback(
                    successText(lang.success.taterzenSkinChange, new LiteralText(skinPlayerName)),
                    false
            );
        } else
            context.getSource().sendError(noSelectedTaterzenError());
        return 0;
    }


    private static int copySkinLayers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_skin_layers)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }
        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            Byte skinLayers = player.getDataTracker().get(getPLAYER_MODEL_PARTS());
            taterzen.getFakePlayer().getDataTracker().set(getPLAYER_MODEL_PARTS(), skinLayers);

            taterzen.sendProfileUpdates();
            context.getSource().sendFeedback(
                    successText(lang.success.skinLayersMirrored, taterzen.getCustomName()),
                    false
            );
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static String addCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_commands_add)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return null;
        }

        TaterzenNPC taterzen = ((TaterzenEditor) context.getSource().getPlayer()).getNpc();
        // Extremely :concern:
        // I know it
        String command = null;
        if(taterzen != null) {
            command = context.getInput().substring(23); // 23 being the length of `/npc edit command add `
            taterzen.addCommand(command);
            // Feedback is sent up above after method call

        } else
            context.getSource().sendError(noSelectedTaterzenError());
        return command;
    }

    private static int removeTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_remove)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        TaterzenNPC taterzen = ((TaterzenEditor) context.getSource().getPlayer()).getNpc();
        if(taterzen != null) {
            taterzen.kill();
            context.getSource().sendFeedback(
                    successText(lang.success.killedTaterzen, taterzen.getCustomName()),
                    false
            );
        } else
            context.getSource().sendError(noSelectedTaterzenError());
        ((TaterzenEditor) context.getSource().getPlayer()).selectNpc(null);
        return 0;
    }

    private static int selectTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_select)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }


        ServerPlayerEntity player = context.getSource().getPlayer();

        Box box = player.getBoundingBox().offset(player.getRotationVector().multiply(2.0D)).expand(0.3D);
        ((TaterzenEditor) player).selectNpc(null);

        player.getEntityWorld().getEntityCollisions(player, box, entity -> {
            if(entity instanceof TaterzenNPC && ((TaterzenEditor) player).getNpc() == null) {
                ((TaterzenEditor) player).selectNpc((TaterzenNPC) entity);
                context.getSource().sendFeedback(
                        successText(lang.success.selectedTaterzen, entity.getCustomName()),
                        false
                );
                return false;
            }
            return true;
        });

        return 0;
    }

    private static int changeType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(!DISGUISELIB_LOADED) {
            context.getSource().sendError(new LiteralText(lang.error.disguiseLibRequired)
                    .formatted(Formatting.RED)
                    .styled(style -> style
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Install DisguiseLib.")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/disguiselib"))
                    )
            );
            return -1;
        }
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_entityType)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
            if(taterzen != null) {

                Identifier disguise = EntitySummonArgumentType.getEntitySummon(context, "entity type");

                CompoundTag nbt;
                try {
                    nbt = NbtCompoundTagArgumentType.getCompoundTag(context, "nbt").copy();
                } catch(IllegalArgumentException ignored) {
                    nbt = new CompoundTag();
                }
                nbt.putString("id", disguise.toString());

                EntityType.loadEntityWithPassengers(nbt, context.getSource().getWorld(), (entityx) -> {
                    DisguiseLibCompatibility.disguiseAs(taterzen, entityx);
                    context.getSource().sendFeedback(
                            successText(lang.success.changedEntityType, new TranslatableText(entityx.getType().getTranslationKey())),
                            false
                    );
                    return entityx;
                });
            } else
                context.getSource().sendError(noSelectedTaterzenError());
        } catch(Error e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static int resetType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(!DISGUISELIB_LOADED) {
            context.getSource().sendError(new LiteralText(lang.error.disguiseLibRequired)
                    .formatted(Formatting.RED)
                    .styled(style -> style
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Install DisguiseLib.")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/disguiselib"))
                    )
            );
            return -1;
        }
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_edit_entityType)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
            if(taterzen != null) {
                DisguiseLibCompatibility.clearDisguise(taterzen);
                context.getSource().sendFeedback(
                        successText(lang.success.resetEntityType, taterzen.getCustomName()),
                        false
                );
            } else
                context.getSource().sendError(noSelectedTaterzenError());
        } catch(Error e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static Collection<String> getOnlinePlayers(CommandContext<ServerCommandSource> context) {
        Collection<String> names = new ArrayList<>();
        context.getSource().getMinecraftServer().getPlayerManager().getPlayerList().forEach(
                player -> names.add(player.getGameProfile().getName())
        );

        return names;
    }

    private static int spawnTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_ENABLED && !checkPermission(context.getSource(), PERMISSIONS.npc_create)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            String taterzenName = MessageArgumentType.getMessage(context, "name").asString();
            TaterzenNPC taterzen = TaterzensAPI.createTaterzen(player, taterzenName);
            player.getEntityWorld().spawnEntity(taterzen);
            ((TaterzenEditor) player).selectNpc(taterzen);
            context.getSource().sendFeedback(
                    successText(lang.success.spawnedTaterzen, taterzen.getCustomName()),
                    false
            );
        } catch(ClassCastException | NoSuchElementException e) {
            e.printStackTrace();
        }
        return 0;
    }

    static {
        MOVEMENT_TYPES = SuggestionProviders.register(
                new Identifier(MODID, "movement_types"),
                (context, builder) ->
                        CommandSource.suggestMatching(Stream.of(NPCData.Movement.values()).map(Enum::name).collect(Collectors.toList()), builder)
        );

        HOSTILITY_TYPES = SuggestionProviders.register(
                new Identifier(MODID, "hostility_types"),
                (context, builder) ->
                        CommandSource.suggestMatching(Stream.of(NPCData.Behaviour.values()).map(Enum::name).collect(Collectors.toList()), builder)
        );
    }
}
