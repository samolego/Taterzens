package org.samo_lego.taterzens.event;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;

public class BlockInteractEvent implements UseBlockCallback {

    public BlockInteractEvent() {
    }

    @Override
    public ActionResult interact(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult blockHitResult) {
        TaterzenEditor player = (TaterzenEditor) playerEntity;
        if(player.getNpc() != null && player.inPathEditMode()) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            player.getNpc().removePathTarget(blockPos);
            if(player instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new BlockUpdateS2CPacket(blockPos, world.getBlockState(blockPos)));
            }
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }
}
