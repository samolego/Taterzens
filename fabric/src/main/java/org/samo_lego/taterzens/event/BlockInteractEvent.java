package org.samo_lego.taterzens.event;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class BlockInteractEvent implements UseBlockCallback {

    public BlockInteractEvent() {
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
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult) {
        return BlockEvent.onBlockInteract(player, world, blockHitResult.getBlockPos());
    }
}
