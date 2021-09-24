package org.samo_lego.taterzens.event;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class BlockInteractEventImpl implements UseBlockCallback {

    public BlockInteractEventImpl() {
    }

    /**
     * Used if player is in path edit mode. Interacted blocks are removed from the path
     * of selected {@link org.samo_lego.taterzens.npc.TaterzenNPC}.
     *
     * @param player player breaking the block
     * @param world world where block is being broken
     * @param blockHitResult hit result to the block
     *
     * @return FAIL if player has selected NPC and is in path edit mode, otherwise PASS.
     */
    @Override
    public InteractionResult interact(Player player, Level world, InteractionHand hand, BlockHitResult blockHitResult) {
        return BlockEvent.onBlockInteract(player, world, blockHitResult.getBlockPos());
    }
}
