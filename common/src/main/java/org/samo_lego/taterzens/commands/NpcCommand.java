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
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
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
import org.samo_lego.taterzens.compatibility.LoaderSpecific;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.NPCData;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.mixin.accessors.PlayerEntityAccessor.getPLAYER_MODEL_PARTS;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class NpcCommand {

    private static final SuggestionProvider<ServerCommandSource> MOVEMENT_TYPES;
    private static final SuggestionProvider<ServerCommandSource> HOSTILITY_TYPES;
    private static final String MINESKIN_API_URL = "https://api.mineskin.org/get/id/";
    private static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();


    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("npc")
                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc, config.perms.npcCommandPermissionLevel))
                .then(literal("create")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_create, config.perms.npcCommandPermissionLevel))
                        .then(argument("name", message())
                                .suggests((context, builder) -> CommandSource.suggestMatching(getOnlinePlayers(context), builder))
                                .executes(NpcCommand::spawnTaterzen)
                        )
                )
                .then(literal("select")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_select, config.perms.npcCommandPermissionLevel))
                        .then(argument("id", IntegerArgumentType.integer(1))
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_select_id, config.perms.npcCommandPermissionLevel))
                                .executes(NpcCommand::selectTaterzenById)
                        )
                        .executes(NpcCommand::selectTaterzen)
                )
                .then(literal("deselect")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_deselect, config.perms.npcCommandPermissionLevel))
                        .executes(NpcCommand::deselectTaterzen)
                )
                .then(literal("list")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_list, config.perms.npcCommandPermissionLevel))
                        .executes(NpcCommand::listTaterzens)
                )
                .then(literal("remove")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_remove, config.perms.npcCommandPermissionLevel))
                        .executes(NpcCommand::removeTaterzen)
                )
                .then(literal("preset")
                        .then(literal("save")
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_preset_save, config.perms.npcCommandPermissionLevel))
                                .then(argument("preset name", word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(getPresets(), builder))
                                        .executes(NpcCommand::saveTaterzenToPreset)
                                )
                        )
                        .then(literal("load")
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_preset_load, config.perms.npcCommandPermissionLevel))
                                .then(argument("preset name", word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(getPresets(), builder))
                                        .executes(NpcCommand::loadTaterzenFromPreset)
                                )
                        )
                )
                .then(literal("tp")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_tp, config.perms.npcCommandPermissionLevel))
                        .then(argument("destination", EntityArgumentType.entity())
                                .executes(context -> teleportTaterzen(context, EntityArgumentType.getEntity(context, "destination").getPos()))
                        )
                        .then(argument("position", Vec3ArgumentType.vec3())
                                .executes(context -> teleportTaterzen(context, Vec3ArgumentType.getPosArgument(context, "position").toAbsolutePos(context.getSource())))
                        )
                )
                .then(literal("edit")
                        .then(literal("name")
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_name, config.perms.npcCommandPermissionLevel))
                                .then(argument("new name", message()).executes(NpcCommand::renameTaterzen))
                        )
                        .then(literal("commands")
                                .then(literal("setPermissionLevel")
                                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_commands_setPermissionLevel, config.perms.npcCommandPermissionLevel))
                                        .then(argument("level", IntegerArgumentType.integer(0, 4))
                                                .executes(NpcCommand::setPermissionLevel)
                                        )
                                )
                                .then(literal("remove")
                                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_commands_remove, config.perms.npcCommandPermissionLevel))
                                        .then(argument("command id", IntegerArgumentType.integer(0)).executes(NpcCommand::removeCommand))
                                )
                                .then(literal("add")
                                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_commands_add, config.perms.npcCommandPermissionLevel))
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
                                .then(literal("clear")
                                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_commands_clear, config.perms.npcCommandPermissionLevel))
                                        .executes(NpcCommand::clearCommands)
                                )
                                .then(literal("list")
                                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_commands_clear, config.perms.npcCommandPermissionLevel))
                                        .executes(NpcCommand::listTaterzenCommands)
                                )
                        )
                        .then(literal("behaviour")
                            .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_behaviour, config.perms.npcCommandPermissionLevel))
                            .then(argument("behaviour", word())
                                    .suggests(HOSTILITY_TYPES)
                                    .executes(NpcCommand::setTaterzenBehaviour)
                            )
                        )
                        .then(literal("invulnerable")
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_tags_invulnerability, config.perms.npcCommandPermissionLevel))
                                .then(argument("invulnerable", BoolArgumentType.bool()).executes(NpcCommand::setInvulnerable))
                        )
                        .then(literal("type")
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_entityType, config.perms.npcCommandPermissionLevel))
                                .then(argument("entity type", EntitySummonArgumentType.entitySummon())
                                        .suggests(SUMMONABLE_ENTITIES)
                                        .executes(NpcCommand::changeType)
                                        .then(argument("nbt", NbtCompoundTagArgumentType.nbtCompound())
                                                .executes(NpcCommand::changeType)
                                        )
                                )
                                .then(literal("minecraft:player")
                                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_entityType, config.perms.npcCommandPermissionLevel))
                                        .executes(NpcCommand::resetType)
                                )
                                .then(literal("player")
                                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_entityType, config.perms.npcCommandPermissionLevel))
                                        .executes(NpcCommand::resetType)
                                )
                                .then(literal("reset")
                                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_entityType, config.perms.npcCommandPermissionLevel))
                                        .executes(NpcCommand::resetType)
                                )
                        )
                        .then(literal("path")
                                .then(literal("clear")
                                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_path_clear, config.perms.npcCommandPermissionLevel))
                                        .executes(NpcCommand::clearTaterzenPath)
                                )
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_path, config.perms.npcCommandPermissionLevel))
                                .executes(NpcCommand::editTaterzenPath)
                        )
                        .then(literal("messages")
                                .then(literal("clear")
                                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_messages_clear, config.perms.npcCommandPermissionLevel))
                                        .executes(NpcCommand::clearTaterzenMessages)
                                )
                                .then(literal("list")
                                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_messages_list, config.perms.npcCommandPermissionLevel))
                                        .executes(NpcCommand::listTaterzenMessages)
                                )
                                .then(argument("message id", IntegerArgumentType.integer(0))
                                        .then(literal("delete")
                                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_messages_delete, config.perms.npcCommandPermissionLevel))
                                                .executes(NpcCommand::deleteTaterzenMessage)
                                        )
                                        .then(literal("setDelay")
                                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_messages_delay, config.perms.npcCommandPermissionLevel))
                                                .then(argument("delay", IntegerArgumentType.integer())
                                                        .executes(NpcCommand::editMessageDelay)
                                                )
                                        )
                                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_messages, config.perms.npcCommandPermissionLevel))
                                        .executes(NpcCommand::editMessage)
                                )
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_messages_edit, config.perms.npcCommandPermissionLevel))
                                .executes(NpcCommand::editTaterzenMessages)
                        )
                        .then(literal("skin")
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_skin, config.perms.npcCommandPermissionLevel))
                                .then(argument("mineskin URL", message())
                                    .executes(NpcCommand::setCustomSkin)
                                )
                                .executes(NpcCommand::copySkinLayers)
                        )
                        .then(literal("equipment")
                                .then(literal("allowEquipmentDrops")
                                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_equipment_equipmentDrops, config.perms.npcCommandPermissionLevel))
                                        .then(argument("drop", BoolArgumentType.bool()).executes(NpcCommand::setEquipmentDrops))
                                )
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_equipment, config.perms.npcCommandPermissionLevel))
                                .executes(NpcCommand::setEquipment)
                        )
                        .then(literal("look")
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_movement, config.perms.npcCommandPermissionLevel))
                                .executes(context -> changeMovement(context, "FORCED_LOOK"))
                        )
                        .then(literal("movement")
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_movement, config.perms.npcCommandPermissionLevel))
                                .then(argument("movement type", word())
                                        .suggests(MOVEMENT_TYPES)
                                        .executes(context -> changeMovement(context, StringArgumentType.getString(context, "movement type")))
                                )
                        )
                        .then(literal("professions")
                            .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_profession, config.perms.npcCommandPermissionLevel))
                            .then(literal("remove")
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_profession_remove, config.perms.npcCommandPermissionLevel))
                                .then(argument("profession type", message())
                                        .suggests(NpcCommand::suggestRemovableProfessions)
                                        .executes(ctx -> removeProfession(ctx, new Identifier(MessageArgumentType.getMessage(ctx, "profession type").asString())))
                                )
                            )
                            .then(literal("add")
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_profession_add, config.perms.npcCommandPermissionLevel))
                                .then(argument("profession type", message())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(PROFESSION_TYPES.keySet().stream().map(Identifier::toString), builder))
                                        .executes(ctx -> setProfession(ctx, new Identifier(MessageArgumentType.getMessage(ctx, "profession type").asString())))
                                )
                            )
                            .then(literal("list")
                                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_profession_list, config.perms.npcCommandPermissionLevel))
                                .executes(NpcCommand::listTaterzenProfessions)
                            )
                        )
                )
        );
    }

    private static int setCustomSkin(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();

        // Shameless self-promotion
        if(config.fabricTailorAdvert) {
            if(FABRICTAILOR_LOADED) {
                source.sendFeedback(new LiteralText(lang.skinCommandUsage)
                                .formatted(Formatting.GOLD)
                                .styled(style ->
                                        style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skin set"))
                                ),
                        false
                );
            } else {
                source.sendFeedback(new LiteralText(lang.fabricTailorAdvert)
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
            String id = MessageArgumentType.getMessage(context, "mineskin URL").getString();
            if(id.contains(":")) {
                THREADPOOL.submit(() -> {
                    String[] params = id.split("/");
                    String mineskinUrl = MINESKIN_API_URL + params[params.length - 1];
                    try {
                        URL url = new URL(mineskinUrl);
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                        connection.setUseCaches(false);
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestMethod("GET");

                        try (
                                InputStream is = connection.getInputStream();
                                InputStreamReader isr = new InputStreamReader(is);
                                BufferedReader br = new BufferedReader(isr);
                        ) {
                            String response = br.readLine();
                            String value = response.split("\"value\":\"")[1].split("\"")[0];
                            String signature = response.split("\"signature\":\"")[1].split("\"")[0];

                            CompoundTag skinTag = new CompoundTag();
                            skinTag.putString("value", value);
                            skinTag.putString("signature", signature);

                            taterzen.setSkinFromTag(skinTag);
                            taterzen.sendProfileUpdates();

                            source.sendFeedback(
                                    successText(lang.success.skinChanged, new LiteralText(id)),
                                    false
                            );
                        }
                    } catch(MalformedURLException e) {
                        source.sendError(errorText(lang.error.malformedUrl, new LiteralText(mineskinUrl)));
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                // Player's name
                GameProfile skinProfile = new GameProfile(null, id);
                skinProfile = SkullBlockEntity.loadProperties(skinProfile);
                taterzen.applySkin(skinProfile);
                context.getSource().sendFeedback(
                        successText(lang.success.skinChanged, new LiteralText(id)),
                        false
                );
            }
        } else
            source.sendError(noSelectedTaterzenError());
        return 0;
    }

    private static int listTaterzenProfessions(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            Collection<Identifier> professionIds = taterzen.getProfessionIds();

            MutableText response = joinText(lang.taterzenProfessions, Formatting.AQUA, taterzen.getCustomName(), Formatting.YELLOW);
            AtomicInteger i = new AtomicInteger();

            professionIds.forEach(identifier -> {
                int index = i.get() + 1;
                response.append(
                        new LiteralText("\n" + index + "-> " + identifier.toString() + " (")
                                .formatted(index % 2 == 0 ? Formatting.YELLOW : Formatting.GOLD)
                                .append(
                                        new LiteralText("X")
                                                .formatted(Formatting.RED)
                                                .formatted(Formatting.BOLD)
                                                .styled(style -> style
                                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Delete " + identifier.getPath())))
                                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc edit professions remove " + identifier))
                                                )
                                )
                                .append(new LiteralText(")").formatted(Formatting.RESET))
                );
                i.incrementAndGet();
            });
            source.sendFeedback(response, false);
        } else
            context.getSource().sendError(noSelectedTaterzenError());
        return 0;
    }

    private static int removeProfession(CommandContext<ServerCommandSource> context, Identifier id) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            if(taterzen.getProfessionIds().contains(id)) {
                taterzen.removeProfession(id);
                source.sendFeedback(successText(lang.success.professionRemoved, new LiteralText(id.toString())), false);
            } else
                context.getSource().sendError(errorText(lang.error.noProfessionFound, new LiteralText(id.toString())));
        } else
            context.getSource().sendError(noSelectedTaterzenError());
        return 0;
    }

    private static CompletableFuture<Suggestions> suggestRemovableProfessions(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        Collection<Identifier> professions = new HashSet<>();
        try {
            TaterzenNPC taterzen = ((TaterzenEditor) ctx.getSource().getPlayer()).getNpc();
            if(taterzen != null) {
                professions = taterzen.getProfessionIds();
            }
        } catch(CommandSyntaxException ignored) {
        }
        return CommandSource.suggestMatching(professions.stream().map(Identifier::toString), builder);
    }

    private static int setProfession(CommandContext<ServerCommandSource> context, Identifier id) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            if(PROFESSION_TYPES.containsKey(id)) {
                taterzen.setProfession(id);
                source.sendFeedback(successText(lang.success.professionAdded, new LiteralText(id.toString())), false);
            } else
                context.getSource().sendError(errorText(lang.error.noProfessionFound, new LiteralText(id.toString())));
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int setEquipmentDrops(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();;
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            boolean drop = BoolArgumentType.getBool(context, "drop");
            taterzen.allowEquipmentDrops(drop);
            source.sendFeedback(successText(lang.success.equipmentDropStatus, new LiteralText(String.valueOf(drop))), false);
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int setInvulnerable(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            boolean invulnerable = BoolArgumentType.getBool(context, "invulnerable");
            taterzen.setInvulnerable(invulnerable);
            source.sendFeedback(successText(lang.success.invulnerableStatus, new LiteralText(String.valueOf(invulnerable))), false);
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int setTaterzenBehaviour(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            NPCData.Behaviour behaviour = NPCData.Behaviour.valueOf(StringArgumentType.getString(context, "behaviour"));
            taterzen.setBehaviour(behaviour);
            source.sendFeedback(successText(lang.success.behaviour, new LiteralText(String.valueOf(behaviour))), false);
            if(behaviour != NPCData.Behaviour.PASSIVE && taterzen.isInvulnerable())
                source.sendFeedback(new LiteralText(lang.success.behaviourSuggestion)
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
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            int selected = IntegerArgumentType.getInteger(context, "command id") - 1;
            if(selected >= taterzen.getCommands().size()) {
                source.sendFeedback(
                        errorText(lang.error.noCommandFound, new LiteralText(String.valueOf(selected))),
                        false
                );
            } else {
                source.sendFeedback(successText(lang.success.commandRemoved, new LiteralText(String.valueOf(taterzen.getCommands().get(selected)))), false);
                taterzen.removeCommand(selected);
            }
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int clearCommands(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            source.sendFeedback(successText(lang.success.commandsCleared, taterzen.getName()), false);
            taterzen.clearCommands();

        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int listTaterzenCommands(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            ArrayList<String> commands = taterzen.getCommands();

            MutableText response = joinText(lang.taterzenCommands, Formatting.AQUA, taterzen.getCustomName(), Formatting.YELLOW);
            if(!commands.isEmpty()) {
                AtomicInteger i = new AtomicInteger();

                commands.forEach(cmd -> {
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

            source.sendFeedback(response, false);
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int setPermissionLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();;

        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            int newPermLevel = IntegerArgumentType.getInteger(context, "level");
            if(!config.perms.allowSettingHigherPermissionLevel && !source.hasPermissionLevel(newPermLevel)) {
                source.sendError(errorText(lang.error.cannotSetPermissionLevel, new LiteralText(String.valueOf(newPermLevel))));
                return -1;
            }
            source.sendFeedback(successText(lang.success.updatedPermissionLevel, new LiteralText(String.valueOf(newPermLevel))), false);
            taterzen.setPermissionLevel(newPermLevel);

        } else
            source.sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int deselectTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ((TaterzenEditor) source.getPlayer()).selectNpc(null);
        source.sendFeedback(new LiteralText(lang.success.deselectedTaterzen).formatted(Formatting.GREEN), false);
        return 0;
    }

    private static int deleteTaterzenMessage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            int selected = IntegerArgumentType.getInteger(context, "message id") - 1;
            if(selected >= taterzen.getMessages().size()) {
                source.sendFeedback(
                        errorText(lang.error.noMessageFound, new LiteralText(String.valueOf(selected))),
                        false
                );
            } else {
                source.sendFeedback(successText(lang.success.messageDeleted, taterzen.getMessages().get(selected).getFirst()), false);
                taterzen.removeMessage(selected);
            }
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int editMessageDelay(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            int selected = IntegerArgumentType.getInteger(context, "message id") - 1;
            if(selected >= taterzen.getMessages().size()) {
                source.sendFeedback(
                        errorText(lang.error.noMessageFound, new LiteralText(String.valueOf(selected))),
                        false
                );
            } else {
                int delay = IntegerArgumentType.getInteger(context, "delay");
                taterzen.setMessageDelay(selected, delay);
                source.sendFeedback(successText(lang.success.messageDelaySet, new LiteralText(String.valueOf(delay))), false);
            }
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int editMessage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            ((TaterzenEditor) source.getPlayer()).setEditorMode(TaterzenEditor.Types.MESSAGES);
            int selected = IntegerArgumentType.getInteger(context, "message id") - 1;
            if(selected >= taterzen.getMessages().size()) {
                source.sendFeedback(
                        successText(lang.error.noMessageFound, new LiteralText(String.valueOf(selected))),
                        false
                );
            } else {
                ((TaterzenEditor) source.getPlayer()).setEditingMessageIndex(selected);
                source.sendFeedback(successText(lang.editMessageMode, taterzen.getMessages().get(selected).getFirst()), false);
            }

        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int listTaterzenMessages(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            ArrayList<Pair<Text, Integer>> messages = taterzen.getMessages();

            MutableText response = joinText(lang.taterzenMessages, Formatting.AQUA, taterzen.getCustomName(), Formatting.YELLOW);
            AtomicInteger i = new AtomicInteger();

            messages.forEach(pair -> {
                int index = i.get() + 1;
                response.append(
                        new LiteralText("\n" + index + "-> ")
                                .formatted(index % 2 == 0 ? Formatting.YELLOW : Formatting.GOLD)
                                .append(pair.getFirst())
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
            source.sendFeedback(response, false);
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int clearTaterzenMessages(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            taterzen.clearMessages();
            source.sendFeedback(successText(lang.success.messagesCleared, taterzen.getName()), false);
        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int editTaterzenMessages(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        ServerPlayerEntity player = source.getPlayer();
        if(taterzen != null) {
            if(((TaterzenEditor) player).getEditorMode() == TaterzenEditor.Types.MESSAGES) {
                // Exiting the message edit mode
                ((TaterzenEditor) player).setEditorMode(TaterzenEditor.Types.NONE);
                ((TaterzenEditor) source.getPlayer()).setEditingMessageIndex(-1);
                context.getSource().sendFeedback(
                        new LiteralText(lang.success.editorExit).formatted(Formatting.LIGHT_PURPLE),
                        false
                );
            } else {
                // Entering the edit mode
                ((TaterzenEditor) player).setEditorMode(TaterzenEditor.Types.MESSAGES);
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
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        ServerPlayerEntity player = source.getPlayer();
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
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        ServerPlayerEntity player = source.getPlayer();
        if(taterzen != null) {
            if(((TaterzenEditor) player).getEditorMode() == TaterzenEditor.Types.PATH) {
                ((TaterzenEditor) player).setEditorMode(TaterzenEditor.Types.NONE);
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

                ((TaterzenEditor) player).setEditorMode(TaterzenEditor.Types.PATH);
            }

        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int loadTaterzenFromPreset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String filename = StringArgumentType.getString(context, "preset name") + ".json";
        File preset = new File(presetsDir + "/" + filename);
        ServerCommandSource source = context.getSource();

        if(preset.exists()) {
            TaterzenNPC taterzenNPC = TaterzensAPI.loadTaterzenFromPreset(preset, source.getWorld());
            if(taterzenNPC != null) {
                Vec3d pos = source.getPosition();
                Vec2f rotation = source.getRotation();
                taterzenNPC.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), rotation.x, rotation.y);

                source.getWorld().spawnEntity(taterzenNPC);

                ((TaterzenEditor) source.getPlayer()).selectNpc(taterzenNPC);

                source.sendFeedback(
                        successText(lang.success.importedTaterzenPreset, new LiteralText(filename)),
                        false
                );
            }
        } else {
            source.sendError(
                    errorText(lang.error.noPresetFound, new LiteralText(filename))
            );
        }
        return 0;
    }

    private static int saveTaterzenToPreset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            String filename = StringArgumentType.getString(context, "preset name") + ".json";
            File preset = new File(presetsDir + "/" + filename);
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
        int id = IntegerArgumentType.getInteger(context, "id");
        ServerCommandSource source = context.getSource();
        if(id > TATERZEN_NPCS.size()) {
            source.sendError(
                    errorText(lang.error.noTaterzenFound, new LiteralText(String.valueOf(id)))
            );
        } else {
            TaterzenNPC taterzen = (TaterzenNPC) TATERZEN_NPCS.toArray()[id - 1];
            ((TaterzenEditor) source.getPlayer()).selectNpc(taterzen);
            source.sendFeedback(
                    successText(lang.success.selectedTaterzen, taterzen.getCustomName()),
                    false
            );
        }
        return 0;
    }

    private static int renameTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
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
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            taterzen.teleport(destination.getX(), destination.getY(), destination.getZ());
        } else
            context.getSource().sendError(noSelectedTaterzenError());
        return 0;
    }

    private static int changeMovement(CommandContext<ServerCommandSource> context, String movement) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
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
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            if(taterzen.isEquipmentEditor(player)) {
                ((TaterzenEditor) player).setEditorMode(TaterzenEditor.Types.NONE);
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
                
                ((TaterzenEditor) player).setEditorMode(TaterzenEditor.Types.EQUIPMENT);
                taterzen.setEquipmentEditor(player);
            }

        } else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int copySkinLayers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            Byte skinLayers = source.getPlayer().getDataTracker().get(getPLAYER_MODEL_PARTS());
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
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        // Extremely :concern:
        // I know it
        String command = null;
        if(taterzen != null) {
            command = context.getInput().substring(23); // 23 being the length of `/npc edit command add `
            taterzen.addCommand(command);
            // Feedback is sent up above after method call

        } else
            source.sendError(noSelectedTaterzenError());
        return command;
    }

    private static int removeTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            taterzen.kill();
            source.sendFeedback(
                    successText(lang.success.killedTaterzen, taterzen.getCustomName()),
                    false
            );
        } else
            source.sendError(noSelectedTaterzenError());
        ((TaterzenEditor) source.getPlayer()).selectNpc(null);
        return 0;
    }

    private static int selectTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Box box = player.getBoundingBox().offset(player.getRotationVector().multiply(2.0D)).expand(0.3D);
        ((TaterzenEditor) source.getPlayer()).selectNpc(null);

        player.getEntityWorld().getOtherEntities(player, box, entity -> {
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
        ServerCommandSource source = context.getSource();
        if(!DISGUISELIB_LOADED) {
            source.sendError(new LiteralText(lang.error.disguiseLibRequired)
                    .formatted(Formatting.RED)
                    .styled(style -> style
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Install DisguiseLib.")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/disguiselib"))
                    )
            );
            return -1;
        }

        try {
            TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
            if(taterzen != null) {

                Identifier disguise = EntitySummonArgumentType.getEntitySummon(context, "entity type");

                CompoundTag nbt;
                try {
                    nbt = NbtCompoundTagArgumentType.getCompoundTag(context, "nbt").copy();
                } catch(IllegalArgumentException ignored) {
                    nbt = new CompoundTag();
                }
                nbt.putString("id", disguise.toString());

                EntityType.loadEntityWithPassengers(nbt, source.getWorld(), (entityx) -> {
                    LoaderSpecific.disguiselib$disguiseAs(taterzen, entityx);
                    source.sendFeedback(
                            successText(lang.success.changedEntityType, new TranslatableText(entityx.getType().getTranslationKey())),
                            false
                    );
                    return entityx;
                });
            } else
                source.sendError(noSelectedTaterzenError());
        } catch(Error e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static int resetType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        if(!DISGUISELIB_LOADED) {
            source.sendError(new LiteralText(lang.error.disguiseLibRequired)
                    .formatted(Formatting.RED)
                    .styled(style -> style
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Install DisguiseLib.")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/disguiselib"))
                    )
            );
            return -1;
        }
        TaterzenNPC taterzen = ((TaterzenEditor) source.getPlayer()).getNpc();
        if(taterzen != null) {
            LoaderSpecific.disguiselib$clearDisguise(taterzen);
            source.sendFeedback(
                    successText(lang.success.resetEntityType, taterzen.getCustomName()),
                    false
            );
        } else
            source.sendError(noSelectedTaterzenError());

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
        ServerPlayerEntity player = context.getSource().getPlayer();
        String taterzenName = MessageArgumentType.getMessage(context, "name").asString();

        TaterzenNPC taterzen = TaterzensAPI.createTaterzen(player, taterzenName);
        player.getEntityWorld().spawnEntity(taterzen);

        ((TaterzenEditor) player).selectNpc(taterzen);
        player.sendMessage(
                successText(lang.success.spawnedTaterzen, taterzen.getCustomName()),
                false
        );

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
