package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;
import org.samo_lego.taterzens.api.TaterzensAPI;
import org.samo_lego.taterzens.commands.edit.EditCommand;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.mixin.accessors.ServerCommandSourceAccessor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.TATERZEN_NPCS;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class NpcCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        LiteralCommandNode<ServerCommandSource> npcNode = dispatcher.register(literal("npc")
                .requires(src -> permissions$checkPermission(src,  "taterzens.npc", config.perms.npcCommandPermissionLevel))
                .then(literal("create")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.create", config.perms.npcCommandPermissionLevel))
                        .then(argument("name", message())
                                .suggests((context, builder) -> CommandSource.suggestMatching(getOnlinePlayers(context), builder))
                                .executes(NpcCommand::spawnTaterzen)
                        )
                        .executes(NpcCommand::spawnTaterzen)
                )
                .then(literal("select")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.select", config.perms.npcCommandPermissionLevel))
                        .then(argument("id", IntegerArgumentType.integer(1))
                                .requires(src -> permissions$checkPermission(src, "taterzens.npc.select.id", config.perms.npcCommandPermissionLevel))
                                .executes(NpcCommand::selectTaterzenById)
                        )
                        .executes(NpcCommand::selectTaterzen)
                )
                .then(literal("deselect")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.select.deselect", config.perms.npcCommandPermissionLevel))
                        .executes(NpcCommand::deselectTaterzen)
                )
                .then(literal("list")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.list", config.perms.npcCommandPermissionLevel))
                        .executes(NpcCommand::listTaterzens)
                )
                .then(literal("remove")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.remove", config.perms.npcCommandPermissionLevel))
                        .executes(NpcCommand::removeTaterzen)
                )
        );

        EditCommand.registerNode(dispatcher, npcNode);
        PresetCommand.registerNode(npcNode);
        TeleportCommand.registerNode(npcNode);
        ActionCommand.registerNode(npcNode);
    }

    /**
     * Error text for no selected taterzen
     * @return formatted error text.
     */
    public static MutableText noSelectedTaterzenError() {
        return translate("taterzens.error.select")
                .formatted(Formatting.RED)
                .styled(style -> style
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
        entity.sendSystemMessage(noSelectedTaterzenError(), entity.getUuid());
        return 0;
    }

    private static int deselectTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ((ITaterzenEditor) source.getPlayer()).selectNpc(null);
        source.sendFeedback(translate("taterzens.command.deselect").formatted(Formatting.GREEN), false);
        return 0;
    }

    private static int listTaterzens(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        boolean console = source.getEntity() == null;
        TaterzenNPC npc = null;

        if(!console) {
            npc = ((ITaterzenEditor) source.getPlayer()).getNpc();
        }

        MutableText response = translate("taterzens.command.list").formatted(Formatting.AQUA);
        Object[] array = TATERZEN_NPCS.toArray();

        for(int i = 0; i < TATERZEN_NPCS.size(); ++i) {
            int index = i + 1;
            TaterzenNPC taterzenNPC = (TaterzenNPC) array[i];
            String name = taterzenNPC.getName().getString();

            boolean sel = taterzenNPC == npc;

            response
                    .append(
                        new LiteralText("\n" + index + "-> " + name)
                            .formatted(sel ? Formatting.BOLD : Formatting.RESET)
                            .formatted(sel ? Formatting.GREEN : (i % 2 == 0 ? Formatting.YELLOW : Formatting.GOLD))
                            .styled(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc select " + index))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate(sel ? "taterzens.tooltip.current_selection" : "taterzens.tooltip.new_selection", name))
                                    )
                            )
                    )
                    .append(
                            new LiteralText(" (" + (console ? taterzenNPC.getUuidAsString() : "uuid") + ")")
                                .formatted(Formatting.GRAY)
                                .styled(style -> style
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.see_uuid")))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, taterzenNPC.getUuidAsString()))
                    )
            );
        }

        source.sendFeedback(response, false);
        return 0;
    }

    private static int selectTaterzenById(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int id = IntegerArgumentType.getInteger(context, "id");
        ServerCommandSource source = context.getSource();
        if(id > TATERZEN_NPCS.size()) {
            source.sendError(errorText("taterzens.error.404.id", String.valueOf(id)));
        } else {
            TaterzenNPC taterzen = (TaterzenNPC) TATERZEN_NPCS.toArray()[id - 1];
            ((ITaterzenEditor) source.getPlayer()).selectNpc(taterzen);
            source.sendFeedback(
                    successText("taterzens.command.select", taterzen.getName().getString()),
                    false
            );
        }
        return 0;
    }


    private static int removeTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        return selectedTaterzenExecutor(player, taterzen -> {
            taterzen.kill();
            source.sendFeedback(
                    successText("taterzens.command.remove", taterzen.getName().getString()),
                    false
            );
            ((ITaterzenEditor) player).selectNpc(null);
        });
    }

    private static int selectTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Box box = player.getBoundingBox().offset(player.getRotationVector().multiply(2.0D)).expand(0.3D);
        ((ITaterzenEditor) player).selectNpc(null);

        player.getEntityWorld().getOtherEntities(player, box, entity -> {
            // null check in order to select first one colliding
            if(entity instanceof TaterzenNPC && ((ITaterzenEditor) player).getNpc() == null) {
                ((ITaterzenEditor) player).selectNpc((TaterzenNPC) entity);
                source.sendFeedback(
                        successText("taterzens.command.select", entity.getName().getString()),
                        false
                );
                return false;
            }
            return true;
        });

        if(((ITaterzenEditor) player).getNpc() == null) {
            source.sendError(
                    translate("taterzens.error.404.detected")
                        .formatted(Formatting.RED)
                        .append("\n")
                        .append(translate("taterzens.command.deselect").formatted(Formatting.GOLD))
            );
        }

        return 0;
    }


    private static Collection<String> getOnlinePlayers(CommandContext<ServerCommandSource> context) {
        Collection<String> names = new ArrayList<>();
        context.getSource().getServer().getPlayerManager().getPlayerList().forEach(
                player -> names.add(player.getGameProfile().getName())
        );

        return names;
    }

    private static int spawnTaterzen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        String taterzenName;
        try {
            taterzenName = MessageArgumentType.getMessage(context, "name").asString();
        } catch(IllegalArgumentException ignored) {
            // no name was provided, defaulting to player's own name
            taterzenName = player.getGameProfile().getName();
        }

        TaterzenNPC taterzen = TaterzensAPI.createTaterzen(player, taterzenName);
        // Making sure permission level is as high as owner's, to prevent permission bypassing.
        taterzen.setPermissionLevel(((ServerCommandSourceAccessor) source).getPermissionLevel());
        player.getEntityWorld().spawnEntity(taterzen);

        ((ITaterzenEditor) player).selectNpc(taterzen);
        player.sendMessage(
                successText("taterzens.command.create", taterzen.getName().getString()),
                false
        );

        return 0;
    }


}
