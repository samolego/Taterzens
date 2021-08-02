package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;

import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class PathCommand {
    public static void registerNode(LiteralCommandNode<ServerCommandSource> editNode) {
        LiteralCommandNode<ServerCommandSource> pathNode = literal("path")
                .then(literal("clear")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.path.clear", config.perms.npcCommandPermissionLevel))
                        .executes(PathCommand::clearTaterzenPath)
                )
                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.path", config.perms.npcCommandPermissionLevel))
                .executes(PathCommand::editTaterzenPath)
                .build();

        editNode.addChild(pathNode);
    }

    private static int clearTaterzenPath(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Entity entity = source.getEntityOrThrow();

        return NpcCommand.selectedTaterzenExecutor(entity, taterzen -> {
            World world = entity.getEntityWorld();
            if(entity instanceof ServerPlayerEntity player)
                taterzen.getPathTargets().forEach(blockPos -> player.networkHandler.sendPacket(
                        new BlockUpdateS2CPacket(blockPos, world.getBlockState(blockPos))
                ));
            taterzen.clearPathTargets();
            context.getSource().sendFeedback(
                    successText("taterzens.command.path_editor.clear", taterzen.getName().getString()),
                    false
            );
        });
    }

    private static int editTaterzenPath(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        return NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
            if(((ITaterzenEditor) player).getEditorMode() == ITaterzenEditor.EditorMode.PATH) {
                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.NONE);
                source.sendFeedback(
                        translate("taterzens.command.equipment.exit").formatted(Formatting.LIGHT_PURPLE),
                        false
                );

            } else {

                source.sendFeedback(
                        joinText("taterzens.command.path_editor.enter", Formatting.LIGHT_PURPLE, Formatting.AQUA, taterzen.getName().getString())
                                .formatted(Formatting.BOLD)
                                .styled(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc edit path"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.exit").formatted(Formatting.RED)))
                                ),
                        false
                );
                source.sendFeedback(
                        translate("taterzens.command.path_editor.desc.1").append("\n").formatted(Formatting.BLUE)
                                .append(translate("taterzens.command.path_editor.desc.2").formatted(Formatting.RED)),
                        false
                );

                ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.PATH);
            }

        });
    }

}
