package org.samo_lego.taterzens.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;

import java.util.ArrayList;

import static org.samo_lego.taterzens.util.TextUtil.errorText;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class BlockEvent {

    /**
     * Used if player is in path edit mode. Interacted blocks are removed from the path
     * of selected {@link org.samo_lego.taterzens.npc.TaterzenNPC}.
     *
     * @param player player breaking the block.
     * @param world world where block is being broken.
     * @param blockPos position of block interaction.
     *
     * @return FAIL if player has selected NPC and is in path edit mode, otherwise PASS.
     */
    public static InteractionResult onBlockInteract(Player player, Level world, BlockPos blockPos)
    {
        ITaterzenEditor editorPlayer = (ITaterzenEditor) player;
        if(editorPlayer.getNpc() != null && ((ITaterzenEditor) player).getEditorMode() == ITaterzenEditor.EditorMode.PATH) {

            ServerPlayer serverPlayer = ((ServerPlayer) editorPlayer);
            ArrayList<BlockPos> pathNodes = editorPlayer.getNpc().getPathTargets();

            if (!pathNodes.isEmpty()) {
                int idx = pathNodes.indexOf(blockPos);
                if (idx >= 0) {
                    editorPlayer.getNpc().removePathTargetByIndex(idx);
                    serverPlayer.connection.send(new ClientboundBlockUpdatePacket(blockPos, world.getBlockState(blockPos)));
                    serverPlayer.sendMessage(successText("taterzens.command.path_editor.remove.success", "(" + blockPos.toShortString() + ")"), ChatType.SYSTEM, serverPlayer.getUUID());
                }
                else {
                    serverPlayer.sendMessage(errorText("taterzens.command.path_editor.remove.404", "(" + blockPos.toShortString() + ")"), ChatType.SYSTEM, serverPlayer.getUUID());
                }
            }
            else {
                serverPlayer.sendMessage(successText("taterzens.command.path_editor.empty", "(" + blockPos.toShortString() + ")"), ChatType.SYSTEM, ((ServerPlayer) editorPlayer).getUUID());
            }
            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }
}