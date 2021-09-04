package org.samo_lego.taterzens.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;

public class BlockEvent {

    /**
     * Used if player is in path edit mode. Interacted blocks are removed from the path
     * of selected {@link org.samo_lego.taterzens.npc.TaterzenNPC}.
     *
     * @param Player player breaking the block.
     * @param world world where block is being broken.
     * @param blockPos position of block interaction.
     *
     * @return FAIL if player has selected NPC and is in path edit mode, otherwise PASS.
     */
    public static InteractionResult onBlockInteract(Player Player, Level world, BlockPos blockPos) {
        if(Player instanceof ServerPlayer) { // Prevents crash on client
            ITaterzenEditor player = (ITaterzenEditor) Player;
            if(player.getNpc() != null && ((ITaterzenEditor) Player).getEditorMode() == ITaterzenEditor.EditorMode.PATH) {
                player.getNpc().removePathTarget(blockPos);
                ((ServerPlayer) player).connection.send(new ClientboundBlockUpdatePacket(blockPos, world.getBlockState(blockPos)));
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }
}