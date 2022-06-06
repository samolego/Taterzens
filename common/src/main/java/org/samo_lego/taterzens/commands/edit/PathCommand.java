package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class PathCommand {
    public static void registerNode(LiteralCommandNode<CommandSourceStack> editNode) {
        LiteralCommandNode<CommandSourceStack> pathNode = literal("path")
            .then(literal("clear")
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.path.clear", config.perms.npcCommandPermissionLevel))
                .executes(PathCommand::clearTaterzenPath)
            )
            .then(literal("list")
                    .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.path.list", config.perms.npcCommandPermissionLevel))
                    .executes(PathCommand::listPathNodes)
            )
            .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.path", config.perms.npcCommandPermissionLevel))
            .then(literal("add")
                .then(argument("pos", BlockPosArgument.blockPos())
                    .suggests((context, builder) -> BlockPosArgument.blockPos().listSuggestions(context, builder))
                    .executes(PathCommand::addPathNode)
                )
            )
            .then(literal("remove")
                .then(literal("index")
                    .then(argument("index", IntegerArgumentType.integer(1))
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(getAvailablePathNodeIndices(context), builder))
                        .executes(PathCommand::removePathNodeByIndex)
                    )
                )
                .executes(PathCommand::removeRecentPathNode)
            )
            .executes(PathCommand::editTaterzenPath)
            .build();

        editNode.addChild(pathNode);
    }

    private static String[] getAvailablePathNodeIndices(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        AtomicReference<ArrayList<BlockPos>> pathNodes = new AtomicReference<>();

        int result = NpcCommand.selectedTaterzenExecutor(player,
            taterzen -> pathNodes.set(taterzen.getPathTargets()));

        if (result == 1)
        {
            String[] availableIndices = new String[pathNodes.get().size()];
            for (int i = 0; i < pathNodes.get().size(); i++) {
                availableIndices[i] = Integer.toString(i + 1);
            }
            return availableIndices;
        }
        else
        {
            return new String[0];
        }
    }
    private static int listPathNodes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        MutableComponent response = translate("taterzens.command.path_editor.list").withStyle(ChatFormatting.AQUA);

        return NpcCommand.selectedTaterzenExecutor(player, taterzen -> {

            ArrayList<BlockPos> pathNodes = taterzen.getPathTargets();
            if (!pathNodes.isEmpty()) {
                for (int i = 0; i < pathNodes.size(); i++)
                {
                    int idx = i + 1;

                    response.append(
                            new TextComponent("\n" + idx + ": (" + pathNodes.get(i).toShortString() + ")")
                                    .withStyle(i % 2 == 0 ? ChatFormatting.GREEN : ChatFormatting.BLUE)
                    );
                }
            }
            else
            {
                response.append(
                        new TextComponent(" " + translate("taterzens.command.path_editor.empty").getString())
                                .withStyle(ChatFormatting.YELLOW)
                );
            }

            source.sendSuccess(response, false);
        });
    }

    private static int addPathNode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");

        int result = NpcCommand.selectedTaterzenExecutor(player, taterzen -> {

            taterzen.addPathTarget(pos);

            // Replace block in world with redstone block in case player is in editor mode
            if(((ITaterzenEditor) player).getEditorMode() == ITaterzenEditor.EditorMode.PATH) {
                player.connection.send(new ClientboundBlockUpdatePacket(pos, Blocks.REDSTONE_BLOCK.defaultBlockState()));
            }
        });

        if (result == 1)
        {
            source.sendSuccess(successText("taterzens.command.path_editor.add.success", "(" + pos.toShortString() + ")"), false);
            return 1;
        }
        else
        {
            source.sendFailure(errorText("taterzens.command.path_editor.add.failure", "(" + pos.toShortString() + ")"));
            return 0;
        }
    }

    private static int removePathNodeByIndex(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        int idx = IntegerArgumentType.getInteger(context, "index") - 1;
        AtomicReference<BlockPos> pathNode = new AtomicReference<>();

        AtomicBoolean success = new AtomicBoolean(false);
        NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
            try
            {
                if (!taterzen.getPathTargets().isEmpty())
                {
                    pathNode.set(taterzen.getPathTargets().get(idx));
                    taterzen.removePathTarget(pathNode.get());

                    // Revert blocks from redstone to before in case player is in editor mode
                    if(((ITaterzenEditor) player).getEditorMode() == ITaterzenEditor.EditorMode.PATH) {
                        player.connection.send(new ClientboundBlockUpdatePacket(pathNode.get(), player.getLevel().getBlockState(pathNode.get())));
                    }

                    source.sendSuccess(successText("taterzens.command.path_editor.remove.success", "(" + pathNode.get().toShortString() + ")"), false);
                }
                else
                {
                    source.sendSuccess(successText("taterzens.command.path_editor.empty"), false);
                }

                success.set(true);
            }
            catch (IndexOutOfBoundsException err)
            {
                source.sendFailure(errorText("taterzens.command.path_editor.remove.outofbounds",
                        Integer.toString(idx + 1),
                        "1",
                        Integer.toString(taterzen.getPathTargets().size()))
                );
                success.set(false);
            }
        });

        if (success.get())
        {
            return 1;
        }
        else
        {
            source.sendFailure(errorText("taterzens.command.path_editor.remove.failure.index", Integer.toString(idx + 1)));
            return 0;
        }
    }

    private static int removeRecentPathNode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        int result = NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
            ArrayList<BlockPos> pathNodes = taterzen.getPathTargets();
            if (!pathNodes.isEmpty())
            {
                int lastIndex = pathNodes.size() - 1;
                BlockPos lastPos = pathNodes.get(lastIndex);
                taterzen.removePathTargetByIndex(lastIndex);

                // Revert blocks from redstone to before in case player is in editor mode
                if(((ITaterzenEditor) player).getEditorMode() == ITaterzenEditor.EditorMode.PATH) {
                    player.connection.send(new ClientboundBlockUpdatePacket(lastPos, player.getLevel().getBlockState(lastPos)));
                }

                source.sendSuccess(successText("taterzens.command.path_editor.remove.success", "(" + lastPos.toShortString() + ")"), false);
            }
            else
            {
                source.sendSuccess(successText("taterzens.command.path_editor.empty"), false);
            }
        });

        if (result == 1)
        {
            return 1;
        }
        else
        {
            source.sendFailure(errorText("taterzens.command.path_editor.remove.failure"));
            return 0;
        }
    }

    private static int clearTaterzenPath(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Entity entity = source.getEntityOrException();

        return NpcCommand.selectedTaterzenExecutor(entity, taterzen -> {
            Level world = entity.getCommandSenderWorld();
            if(entity instanceof ServerPlayer player)
                taterzen.getPathTargets().forEach(blockPos -> player.connection.send(
                        new ClientboundBlockUpdatePacket(blockPos, world.getBlockState(blockPos))
                ));
            taterzen.clearPathTargets();
            context.getSource().sendSuccess(
                    successText("taterzens.command.path_editor.clear", taterzen.getName().getString()),
                    false
            );
        });
    }

    private static int editTaterzenPath(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        return NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
            if(((ITaterzenEditor) player).getEditorMode() == ITaterzenEditor.EditorMode.PATH) {
                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.NONE);
                source.sendSuccess(
                        translate("taterzens.command.equipment.exit").withStyle(ChatFormatting.LIGHT_PURPLE),
                        false
                );

            } else {

                source.sendSuccess(
                        joinText("taterzens.command.path_editor.enter", ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA, taterzen.getName().getString())
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit path"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.exit").withStyle(ChatFormatting.RED)))
                                ),
                        false
                );
                source.sendSuccess(
                        translate("taterzens.command.path_editor.desc.1").append("\n").withStyle(ChatFormatting.BLUE)
                                .append(translate("taterzens.command.path_editor.desc.2").withStyle(ChatFormatting.RED)),
                        false
                );

                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.PATH);
            }

        });
    }

}
