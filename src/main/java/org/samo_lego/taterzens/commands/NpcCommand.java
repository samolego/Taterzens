package org.samo_lego.taterzens.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.JsonOps;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.NPCData;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.entity.EntityType.loadEntityWithPassengers;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.*;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class NpcCommand {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final JsonParser parser = new JsonParser();
    private static final File PRESETS_DIR = new File(getTaterDir() + "/presets/");

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
                        .then(literal("id")
                                .then(argument("id", IntegerArgumentType.integer(1))
                                    .executes(NpcCommand::selectTaterzenById)
                                )
                        )
                        .executes(NpcCommand::selectTaterzen)
                )
                .then(literal("list")
                    .executes(NpcCommand::listTaterzens)
                )
                .then(literal("remove")
                        .executes(NpcCommand::removeTaterzen)
                )
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
                                    .executes(NpcCommand::loadTaterzenFromPrreset)
                                )
                        )
                )
                .then(literal("tp")
                        .then(argument("destination", EntityArgumentType.entity())
                            .executes(context -> teleportTaterzen(context,  EntityArgumentType.getEntity(context, "destination").getPos()))
                        )
                        .then(argument("position", Vec3ArgumentType.vec3())
                            .executes(context -> teleportTaterzen(context, Vec3ArgumentType.getPosArgument(context, "position").toAbsolutePos(context.getSource())))
                        )
                )
                .then(literal("edit")
                    .then(literal("name")
                            .then(argument("new name", message())
                                    .executes(NpcCommand::renameTaterzen)
                            )
                    )
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
                    .then(literal("movement") //todo create from enum
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

    private static List<String> getPresets() {
        List<String> files = new ArrayList<>();
        Arrays.stream(PRESETS_DIR.listFiles()).forEach(file -> {
            if(file.isFile() && file.getName().endsWith(".json"))
                files.add(file.getName());
        });

        return files;
    }

    private static int loadTaterzenFromPrreset(CommandContext<ServerCommandSource> context) {

        String filename = StringArgumentType.getString(context, "preset name") + ".json";
        File preset = new File(PRESETS_DIR + "/" + filename);

        if(preset.exists()) {
            JsonElement element;
            try (BufferedReader fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(preset), StandardCharsets.UTF_8)
                )
            ) {
                element = parser.parse(fileReader).getAsJsonObject();
            } catch (IOException e) {
                throw new RuntimeException(MODID + " Problem occurred when trying to load Taterzen preset: ", e);
            }
            if(element != null) {
                try {
                    Tag tag = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, element);
                    if(tag instanceof CompoundTag) {
                        ServerPlayerEntity player = context.getSource().getPlayer();

                        TaterzenNPC taterzenNPC = new TaterzenNPC(player, filename);
                        taterzenNPC.readCustomDataFromTag((CompoundTag) tag);
                        taterzenNPC.sendProfileUpdates();

                        ((TaterzenEditor) player).selectNpc(taterzenNPC);

                        context.getSource().sendFeedback(
                                successText(lang.success.importedTaterzenPreset, new LiteralText(filename)),
                                false
                        );
                    }
                    else {
                        context.getSource().sendError(
                                errorText(lang.error.cannotReadPreset, new LiteralText(filename))
                        );
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }
            else {
                context.getSource().sendError(
                        errorText(lang.error.cannotReadPreset, new LiteralText(filename))
                );
            }

        }
        else {
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
            CompoundTag saveTag = new CompoundTag();
            taterzen.writeCustomDataToTag(saveTag);
            //todo Weird as it is, those cannot be read back :(
            saveTag.remove("ArmorDropChances");
            saveTag.remove("HandDropChances");

            TATERZEN_NPCS.add(taterzen); // When writing to tag, it was removed so we add it back

            JsonElement element = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, saveTag);

            File preset = new File(PRESETS_DIR + "/" + filename);
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(preset), StandardCharsets.UTF_8)) {
                writer.write(gson.toJson(element));
            } catch (IOException e) {
                getLogger().error("Problem occurred when saving Taterzen preset file: " + e.getMessage());
            }

            context.getSource().sendFeedback(
                    successText(lang.success.exportedTaterzen, new LiteralText(filename)),
                    false
            );
        }
        else
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
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc select id " + index))
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
        }
        else {
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
        }
        else
            context.getSource().sendError(noSelectedTaterzenError());

        return 0;
    }

    private static int teleportTaterzen(CommandContext<ServerCommandSource> context, Vec3d destination) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        TaterzenNPC taterzen = ((TaterzenEditor) player).getNpc();
        if(taterzen != null) {
            taterzen.teleport(destination.getX(), destination.getY(), destination.getZ());
        }
        else
            context.getSource().sendError(noSelectedTaterzenError());
        return 0;
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
            }
            else {
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

        }
        else
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
            taterzen.applySkin(skinProfile, true);
            context.getSource().sendFeedback(
                    successText(lang.success.taterzenSkinChange, new LiteralText(skinPlayerName)),
                    false
            );
        }
        else
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

        }
        else
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
        }
        else
            context.getSource().sendError(noSelectedTaterzenError());
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
                        successText(lang.success.selectedTaterzen, entity.getCustomName()),
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
                        noSelectedTaterzenError()
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
                player -> names.add( player.getGameProfile().getCustomName() )
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
                    successText(lang.success.spawnedTaterzen, taterzen.getCustomName()),
                    false
            );
        } catch (ClassCastException | NoSuchElementException e) {
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
}
