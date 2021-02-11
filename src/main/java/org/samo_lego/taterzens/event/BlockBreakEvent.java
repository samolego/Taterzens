package org.samo_lego.taterzens.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;

public class BlockBreakEvent implements PlayerBlockBreakEvents.Before {

    public BlockBreakEvent() {
    }

    @Override
    public boolean beforeBlockBreak(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        TaterzenEditor player = (TaterzenEditor) playerEntity;
        if(player.getNpc() != null && player.inPathEditMode()) {
            player.getNpc().addPathTarget(blockPos);
            if(player instanceof ServerPlayerEntity)
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new BlockUpdateS2CPacket(blockPos, Blocks.GOLD_BLOCK.getDefaultState()));
            return false;
        }
        return true;
    }
}
