package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.api.TaterzensAPI;
import org.samo_lego.taterzens.commands.edit.EditCommand;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.mixin.accessors.CommandSourceStackAccessor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.MessageArgument.message;
import static org.samo_lego.taterzens.Taterzens.TATERZEN_NPCS;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.errorText;
import static org.samo_lego.taterzens.util.TextUtil.successText;
import static org.samo_lego.taterzens.util.TextUtil.translate;

public class NpcCommand {
    public static LiteralCommandNode<CommandSourceStack> npcNode;

    private static final double MAX_DISTANCE = 8.02;
    private static final double SQRD_DIST = MAX_DISTANCE * MAX_DISTANCE;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        npcNode = dispatcher.register(literal("npc")
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src,  "taterzens.npc", config.perms.npcCommandPermissionLevel))
                .then(literal("create")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.create", config.perms.npcCommandPermissionLevel))
                        .then(argument("name", message())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(getOnlinePlayers(context), builder))
                                .executes(NpcCommand::spawnTaterzen)
                        )
                        .executes(NpcCommand::spawnTaterzen)
                )
                .then(literal("select")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.select", config.perms.npcCommandPermissionLevel))
                        .then(literal("id")
                            .then(argument("id", IntegerArgumentType.integer(1))
                                    .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.select.id", config.perms.npcCommandPermissionLevel))
                                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(getAvailableTaterzenIndices(), builder))
                                    .executes(NpcCommand::selectTaterzenById)
                            )
                        )
                        .then(literal("name")
                            .then(argument("name", StringArgumentType.string())
                                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.select.name", config.perms.npcCommandPermissionLevel))
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(getAvailableTaterzenNames(), builder))
                                .executes(NpcCommand::selectTaterzenByName)
                            )
                        )
                        .then(literal("uuid")
                            .then(argument("uuid", StringArgumentType.string())
                                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.select.uuid", config.perms.npcCommandPermissionLevel))
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(getAvailableTaterzenUUIDs(), builder))
                                .executes(NpcCommand::selectTaterzenByUUID)
                            )
                        )
                        .executes(NpcCommand::selectTaterzen)
                )
                .then(literal("deselect")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.select.deselect", config.perms.npcCommandPermissionLevel))
                        .executes(NpcCommand::deselectTaterzen)
                )
                .then(literal("list")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.list", config.perms.npcCommandPermissionLevel))
                        .executes(NpcCommand::listTaterzens)
                )
                .then(literal("remove")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.remove", config.perms.npcCommandPermissionLevel))
                        .executes(NpcCommand::removeTaterzen)
                )
        );

        EditCommand.registerNode(dispatcher, npcNode);
        PresetCommand.registerNode(npcNode);
        TeleportCommand.registerNode(npcNode);
        ActionCommand.registerNode(npcNode);
        LockCommand.registerNode(npcNode);
    }

    /**
     * Error text for no selected taterzen
     * @return formatted error text.
     */
    public static MutableComponent noSelectedTaterzenError() {
        return translate("taterzens.error.select")
                .withStyle(ChatFormatting.RED)
                .withStyle(style -> style
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.command.list")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc list"))
                );
    }

    /**
     * Gets player's selected Taterzen and executes the consumer.
     * @param entity player to get Taterzen for or Taterzen itself
     * @param npcConsumer lambda that gets selected Taterzen as argument
     * @return 1 if player has npc selected and predicate test passed, otherwise 0
     */
    public static int selectedTaterzenExecutor(@NotNull Entity entity, Consumer<TaterzenNPC> npcConsumer) {
        TaterzenNPC taterzen = null;
        if(entity instanceof ITaterzenEditor player)
            taterzen = player.getNpc();
        else if(entity instanceof TaterzenNPC taterzenNPC)
            taterzen = taterzenNPC;
        if(taterzen != null) {
            npcConsumer.accept(taterzen);
            return 1;
        }
        entity.sendSystemMessage(noSelectedTaterzenError());
        return 0;
    }

    private static int deselectTaterzen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ((ITaterzenEditor) player).selectNpc(null);
        source.sendSuccess(translate("taterzens.command.deselect").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int listTaterzens(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        boolean console = source.getEntity() == null;
        TaterzenNPC npc = null;

        if (!console) {
            npc = ((ITaterzenEditor) source.getPlayerOrException()).getNpc();
        }

        MutableComponent response = translate("taterzens.command.list").withStyle(ChatFormatting.AQUA);

        int i = 1;
        for (var taterzenNPC : TATERZEN_NPCS.values()) {
            String name = taterzenNPC.getName().getString();

            boolean sel = taterzenNPC == npc;

            response.append(
                            Component.literal("\n" + i + "-> " + name)
                                    .withStyle(sel ? ChatFormatting.BOLD : ChatFormatting.RESET)
                                    .withStyle(sel ? ChatFormatting.GREEN : (i % 2 == 0 ? ChatFormatting.YELLOW : ChatFormatting.GOLD))
                                    .withStyle(style -> style
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc select uuid " + taterzenNPC.getUUID().toString()))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate(sel ? "taterzens.tooltip.current_selection" : "taterzens.tooltip.new_selection", name)))))
                    .append(
                            Component.literal(" (" + (console ? taterzenNPC.getStringUUID() : "uuid") + ")")
                                    .withStyle(ChatFormatting.GRAY)
                                    .withStyle(style -> style
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.see_uuid")))
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, taterzenNPC.getStringUUID()))
                                    )
                    );
            ++i;
        }

        source.sendSuccess(response, false);
        return 1;
    }

    private static List<String> getAvailableTaterzenIndices() {
        return IntStream.range(0, TATERZEN_NPCS.size())
                .mapToObj(i -> String.valueOf(i + 1))
                .toList();
    }

    private static int selectTaterzenById(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int id = IntegerArgumentType.getInteger(context, "id");
        CommandSourceStack source = context.getSource();
        if (id > TATERZEN_NPCS.size()) {
            source.sendFailure(errorText("taterzens.error.404.id", String.valueOf(id)));
        } else {
            TaterzenNPC taterzen = (TaterzenNPC) TATERZEN_NPCS.values().toArray()[id - 1];
            ServerPlayer player = source.getPlayerOrException();
            TaterzenNPC npc = ((ITaterzenEditor) player).getNpc();

            if (npc != null) {
                ((ITaterzenEditor) player).selectNpc(null);
            }

            boolean selected = ((ITaterzenEditor) player).selectNpc(taterzen);
            if (selected) {
                source.sendSuccess(
                        successText("taterzens.command.select", taterzen.getName().getString()),
                        false
                );
            } else {
                source.sendFailure(
                        errorText("taterzens.command.error.locked", taterzen.getName().getString())
                );
            }
        }
        return 1;
    }

    private static List<String> getAvailableTaterzenNames() {
        // Adds quotation marks to the suggested name, such that
        // Names containing a whitespace character (ex. the
        // name is 'Foo Bar') can be completed and correctly
        // used without the user having to enclose the argument
        // name with quotation marks themselves.
        return TATERZEN_NPCS.values().stream().map(npc -> "\"" + npc.getName().getString() + "\"").toList();
    }

    private static int selectTaterzenByName(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String name = StringArgumentType.getString(context, "name");

        // first count how many NPCs have the same name.
        // If there is more than one NPC with an identical name, the command will fail.
        // Otherwise, the NPC will be selected.
        // In case no NPC with that name is found, taterzen is null
        TaterzenNPC taterzen = null;
        int count = 0; // number of npcs with identical name
        for (TaterzenNPC npcIt : TATERZEN_NPCS.values()) {
            if (npcIt.getName().getString().equals(name)) {
                taterzen = npcIt;
                count++;

                if (count > 1) {
                    source.sendFailure(errorText("taterzens.error.multiple.name", name));
                    return 0;
                }
            }
        }

        if (count == 0) { // equivalent to taterzen == null
            source.sendFailure(errorText("taterzens.error.404.name", name));
            return 0;
        } else { // if count == 1
            ServerPlayer player = source.getPlayerOrException();
            TaterzenNPC npc = ((ITaterzenEditor) player).getNpc();
            if (npc != null) {
                ((ITaterzenEditor) player).selectNpc(null);
            }
            boolean selected = ((ITaterzenEditor) player).selectNpc(taterzen);
            if (selected) {
                source.sendSuccess(
                        successText("taterzens.command.select", taterzen.getName().getString()),
                        false
                );
                return 1;
            } else {
                source.sendFailure(
                        errorText("taterzens.command.error.locked", taterzen.getName().getString())
                );
                return 0;
            }
        }
    }

    private static List<String> getAvailableTaterzenUUIDs() {
        return TATERZEN_NPCS.keySet().stream().map(UUID::toString).toList();
    }

    private static int selectTaterzenByUUID(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        UUID uuid;
        try {
            uuid = UUID.fromString(StringArgumentType.getString(context, "uuid"));
        } catch (IllegalArgumentException ex) {
            source.sendFailure(errorText("argument.uuid.invalid"));
            return 0;
        }

        TaterzenNPC taterzen = TATERZEN_NPCS.get(uuid);

        if (taterzen == null) {
            source.sendFailure(errorText("taterzens.error.404.uuid", uuid.toString()));
            return 0;
        } else {
            ServerPlayer player = source.getPlayerOrException();
            TaterzenNPC npc = ((ITaterzenEditor) player).getNpc();
            if (npc != null) {
                ((ITaterzenEditor) player).selectNpc(null);
            }
            boolean selected = ((ITaterzenEditor) player).selectNpc(taterzen);
            if (selected) {
                source.sendSuccess(
                        successText("taterzens.command.select", taterzen.getName().getString()),
                        false
                );
                return 1;
            } else {
                source.sendFailure(
                        errorText("taterzens.command.error.locked", taterzen.getName().getString())
                );
                return 0;
            }
        }
    }


    private static int removeTaterzen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        return selectedTaterzenExecutor(player, taterzen -> {
            taterzen.kill();
            source.sendSuccess(
                    successText("taterzens.command.remove", taterzen.getName().getString()),
                    false
            );
            ((ITaterzenEditor) player).selectNpc(null);
        });
    }

    private static int selectTaterzen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        // Made with help of https://github.com/Patbox/polydex/blob/2e1cd03470c6202bf0522c845caa35b20244f8b9/src/main/java/eu/pb4/polydex/impl/display/PolydexTargetImpl.java#L44
        Vec3 min = player.getEyePosition(0);

        Vec3 vec3d2 = player.getViewVector(1.0F);
        Vec3 max = min.add(vec3d2.x * MAX_DISTANCE, vec3d2.y * MAX_DISTANCE, vec3d2.z * MAX_DISTANCE);
        AABB box = player.getBoundingBox().expandTowards(vec3d2.scale(MAX_DISTANCE)).inflate(1.0D);

        final var hit = ProjectileUtil.getEntityHitResult(player, min, max, box, entity -> entity.isPickable() && entity instanceof TaterzenNPC, SQRD_DIST);

        if (hit != null) {
            TaterzenNPC taterzen = (TaterzenNPC) hit.getEntity();
            boolean selected = ((ITaterzenEditor) player).selectNpc(taterzen);
            if (selected) {
                source.sendSuccess(
                        successText("taterzens.command.select", taterzen.getName().getString()),
                        false
                );
            } else {
                source.sendFailure(
                        errorText("taterzens.command.error.locked", taterzen.getName().getString())
                );
            }
        } else {
            source.sendFailure(
                    translate("taterzens.error.404.detected")
                            .withStyle(ChatFormatting.RED)
            );
        }
        return 1;
    }


    private static Collection<String> getOnlinePlayers(CommandContext<CommandSourceStack> context) {
        Collection<String> names = new ArrayList<>();
        context.getSource().getServer().getPlayerList().getPlayers().forEach(
                player -> names.add(player.getGameProfile().getName())
        );

        return names;
    }

    private static int spawnTaterzen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        TaterzenNPC npc = ((ITaterzenEditor) player).getNpc();
        if(npc != null) {
            ((ITaterzenEditor) player).selectNpc(null);
        }

        String taterzenName;
        try {
            taterzenName = MessageArgument.getMessage(context, "name").getString();
        } catch(IllegalArgumentException ignored) {
            // no name was provided, defaulting to player's own name
            taterzenName = player.getGameProfile().getName();
        }

        TaterzenNPC taterzen = TaterzensAPI.createTaterzen(player, taterzenName);
        // Making sure permission level is as high as owner's, to prevent permission bypassing.
        taterzen.setPermissionLevel(((CommandSourceStackAccessor) source).getPermissionLevel());

        // Lock if needed
        if (config.lockAfterCreation)
            taterzen.setLocked(player);

        player.getLevel().addFreshEntity(taterzen);

        ((ITaterzenEditor) player).selectNpc(taterzen);
        player.sendSystemMessage(successText("taterzens.command.create", taterzen.getName().getString()));

        return 1;
    }
}
