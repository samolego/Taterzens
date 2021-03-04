package org.samo_lego.taterzens.commands;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.samo_lego.taterzens.api.TaterzensAPI;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.NPCData;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import xyz.nucleoid.disguiselib.EntityDisguise;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.entity.EntityType.FISHING_BOBBER;
import static net.minecraft.entity.EntityType.ITEM;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.*;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class NpcCommand {

    private static final JsonParser parser = new JsonParser();
    private static final File PRESETS_DIR = new File(getTaterDir() + "/presets/");
    private static final SuggestionProvider<ServerCommandSource> ENTITIES;
    private static final SuggestionProvider<ServerCommandSource> MOVEMENT_TYPES;


    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("npc")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
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
                        .then(literal("command")
                                .redirect(dispatcher.getRoot(), context -> {
                                    // Really ugly, but ... works :P
                                    String cmd = setCommand(context);
                                    throw new SimpleCommandExceptionType(
                                            cmd == null ?
                                                    noSelectedTaterzenError() :
                                                    joinString(lang.success.setCommandAction, Formatting.GOLD, "/" + cmd, Formatting.GRAY)
                                    ).create();
                                })
                        )
                        .then(literal("type")
                                .then(argument("entity type", greedyString())
                                        .suggests(ENTITIES)
                                        .executes(NpcCommand::changeType)
                                )
                        )
                        .then(literal("path").executes(NpcCommand::editTaterzenPath)
                            .then(literal("clear").executes(NpcCommand::clearTaterzenPath))
                        )
                        .then(literal("skin").then(argument("player name", word()).executes(NpcCommand::setSkin)))
                        .then(literal("equipment").executes(NpcCommand::setEquipment))
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

    private static int clearTaterzenPath(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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

    private static List<String> getPresets() {
        List<String> files = new ArrayList<>();
        Arrays.stream(PRESETS_DIR.listFiles()).forEach(file -> {
            if(file.isFile() && file.getName().endsWith(".json"))
                files.add(file.getName().substring(0, file.getName().length() - 5));
        });

        return files;
    }

    private static int loadTaterzenFromPreset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        String filename = StringArgumentType.getString(context, "preset name") + ".json";
        File preset = new File(PRESETS_DIR + "/" + filename);

        if(preset.exists()) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            TaterzenNPC taterzenNPC = TaterzensAPI.loadTaterzenFromPreset(preset, player.getEntityWorld());
            assert taterzenNPC != null;
            taterzenNPC.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.yaw, player.pitch);
            player.getEntityWorld().spawnEntity(taterzenNPC);
            ((TaterzenEditor) player).selectNpc(taterzenNPC);

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
        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            String filename = StringArgumentType.getString(context, "preset name") + ".json";
            File preset = new File(PRESETS_DIR + "/" + filename);
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
            Text name = TATERZEN_NPCS.get(i).getCustomName();
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
        ServerPlayerEntity player = context.getSource().getPlayer();
        int id = IntegerArgumentType.getInteger(context, "id");
        if(id > TATERZEN_NPCS.size()) {
            context.getSource().sendError(
                    errorText(lang.error.noTaterzenFound, new LiteralText(String.valueOf(id)))
            );
        } else {
            TaterzenNPC taterzen = TATERZEN_NPCS.get(id - 1);
            ((TaterzenEditor) player).selectNpc(taterzen);
            context.getSource().sendFeedback(
                    successText(lang.success.selectedTaterzen, taterzen.getCustomName()),
                    false
            );
        }
        return 0;
    }

    private static int renameTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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
        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            taterzen.teleport(destination.getX(), destination.getY(), destination.getZ());
        } else
            context.getSource().sendError(noSelectedTaterzenError());
        return 0;
    }

    private static int changeMovement(CommandContext<ServerCommandSource> context, String movement) throws CommandSyntaxException {
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
        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
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

    private static String setCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TaterzenNPC taterzen = ((TaterzenEditor) context.getSource().getPlayer()).getNpc();
        // Extremely :concern:
        // I know it
        String command = null;
        if(taterzen != null) {
            command = context.getInput().substring(18); // 18 being the length of `/npc edit command `
            taterzen.setCommand(command);

        } else
            context.getSource().sendError(noSelectedTaterzenError());
        return command;
    }

    private static int removeTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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
        ServerPlayerEntity player = context.getSource().getPlayer();

        Box box = player.getBoundingBox().offset(player.getRotationVector()).expand(4.0D);
        player.getEntityWorld().getEntityCollisions(player, box, entity -> {
            if(entity instanceof TaterzenNPC) {
                ((TaterzenEditor) player).selectNpc((TaterzenNPC) entity);
                context.getSource().sendFeedback(
                        successText(lang.success.selectedTaterzen, entity.getCustomName()),
                        false
                );
                return true;
            }
            return false;
        });

        return 0;
    }

    private static int changeType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
            if(taterzen != null) {
                String entityId = StringArgumentType.getString(context, "entity type");
                Optional<EntityType<?>> optionalType = EntityType.get(entityId);
                if(optionalType.isPresent()) {
                    ((EntityDisguise) taterzen).disguiseAs(optionalType.get());
                    context.getSource().sendFeedback(
                            joinString(lang.success.changedEntityType, Formatting.GREEN, entityId, Formatting.YELLOW),
                            false
                    );
                } else {
                    context.getSource().sendError(errorText(lang.error.invalidEntityId, new LiteralText(entityId)));
                }

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
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            String taterzenName = MessageArgumentType.getMessage(context, "name").asString();
            TaterzenNPC taterzen = new TaterzenNPC(player, taterzenName);
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

    private static MutableText noSelectedTaterzenError() {
        return new LiteralText(lang.error.selectTaterzen)
                .formatted(Formatting.RED)
                .styled(style -> style
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(lang.showLoadedTaterzens)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc list"))
                );
    }

    static {
        ENTITIES = SuggestionProviders.register(
                new Identifier("taterzens", "entites"),
                (context, builder) ->
                        CommandSource.suggestFromIdentifier(Registry.ENTITY_TYPE.stream().filter(type -> type != FISHING_BOBBER || type != ITEM), builder, EntityType::getId,
                                (entityType) -> new TranslatableText(Util.createTranslationKey("entity", EntityType.getId(entityType)))
                        )
        );

        MOVEMENT_TYPES = SuggestionProviders.register(
                new Identifier(MODID, "movement_types"),
                (context, builder) ->
                        CommandSource.suggestMatching(Stream.of(NPCData.Movement.values()).map(Enum::name).collect(Collectors.toList()), builder)
        );
    }
}
