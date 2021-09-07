package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.samo_lego.taterzens.api.TaterzensAPI;
import org.samo_lego.taterzens.commands.edit.EditCommand;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.mixin.accessors.CommandSourceStackAccessor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.MessageArgument.message;
import static org.samo_lego.taterzens.Taterzens.TATERZEN_NPCS;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class NpcCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        LiteralCommandNode<CommandSourceStack> npcNode = dispatcher.register(literal("npc")
                .requires(src -> permissions$checkPermission(src,  "taterzens.npc", config.perms.npcCommandPermissionLevel))
                .then(literal("create")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.create", config.perms.npcCommandPermissionLevel))
                        .then(argument("name", message())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(getOnlinePlayers(context), builder))
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
        entity.sendMessage(noSelectedTaterzenError(), entity.getUUID());
        return 0;
    }

    private static int deselectTaterzen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ((ITaterzenEditor) source.getPlayerOrException()).selectNpc(null);
        source.sendSuccess(translate("taterzens.command.deselect").withStyle(ChatFormatting.GREEN), false);
        return 0;
    }

    private static int listTaterzens(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        boolean console = source.getEntity() == null;
        TaterzenNPC npc = null;

        if(!console) {
            npc = ((ITaterzenEditor) source.getPlayerOrException()).getNpc();
        }

        MutableComponent response = translate("taterzens.command.list").withStyle(ChatFormatting.AQUA);
        Object[] array = TATERZEN_NPCS.toArray();

        for(int i = 0; i < TATERZEN_NPCS.size(); ++i) {
            int index = i + 1;
            TaterzenNPC taterzenNPC = (TaterzenNPC) array[i];
            String name = taterzenNPC.getName().getString();

            boolean sel = taterzenNPC == npc;

            response
                    .append(
                        new TextComponent("\n" + index + "-> " + name)
                            .withStyle(sel ? ChatFormatting.BOLD : ChatFormatting.RESET)
                            .withStyle(sel ? ChatFormatting.GREEN : (i % 2 == 0 ? ChatFormatting.YELLOW : ChatFormatting.GOLD))
                            .withStyle(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc select " + index))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate(sel ? "taterzens.tooltip.current_selection" : "taterzens.tooltip.new_selection", name))
                                    )
                            )
                    )
                    .append(
                            new TextComponent(" (" + (console ? taterzenNPC.getStringUUID() : "uuid") + ")")
                                .withStyle(ChatFormatting.GRAY)
                                .withStyle(style -> style
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.see_uuid")))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, taterzenNPC.getStringUUID()))
                    )
            );
        }

        source.sendSuccess(response, false);
        return 0;
    }

    private static int selectTaterzenById(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int id = IntegerArgumentType.getInteger(context, "id");
        CommandSourceStack source = context.getSource();
        if(id > TATERZEN_NPCS.size()) {
            source.sendFailure(errorText("taterzens.error.404.id", String.valueOf(id)));
        } else {
            TaterzenNPC taterzen = (TaterzenNPC) TATERZEN_NPCS.toArray()[id - 1];
            ServerPlayer player = source.getPlayerOrException();
            TaterzenNPC npc = ((ITaterzenEditor) player).getNpc();
            if(npc != null) {
                ((ITaterzenEditor) player).selectNpc(null);
                npc.sendProfileUpdates();
            }
            ((ITaterzenEditor) player).selectNpc(taterzen);
            taterzen.sendProfileUpdates();
            source.sendSuccess(
                    successText("taterzens.command.select", taterzen.getName().getString()),
                    false
            );
        }
        return 0;
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

        AABB box = player.getBoundingBox().move(player.getLookAngle().scale(2.0F)).inflate(0.3D);

        TaterzenNPC npc = ((ITaterzenEditor) player).getNpc();
        if(npc != null) {
            ((ITaterzenEditor) player).selectNpc(null);
            npc.sendProfileUpdates();
        }

        player.getLevel().getEntities(player, box, entity -> {
            // null check in order to select first one colliding
            if(entity instanceof TaterzenNPC taterzen && ((ITaterzenEditor) player).getNpc() == null) {
                ((ITaterzenEditor) player).selectNpc(taterzen);
                taterzen.sendProfileUpdates();
                source.sendSuccess(
                        successText("taterzens.command.select", entity.getName().getString()),
                        false
                );
                return false;
            }
            return true;
        });

        if(((ITaterzenEditor) player).getNpc() == null) {
            source.sendFailure(
                    translate("taterzens.error.404.detected")
                        .withStyle(ChatFormatting.RED)
                        .append("\n")
                        .append(translate("taterzens.command.deselect").withStyle(ChatFormatting.GOLD))
            );
        }

        return 0;
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
            npc.sendProfileUpdates();
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
        player.getLevel().addFreshEntity(taterzen);

        ((ITaterzenEditor) player).selectNpc(taterzen);
        player.sendMessage(
                successText("taterzens.command.create", taterzen.getName().getString()),
                player.getUUID()
        );

        return 0;
    }


}
