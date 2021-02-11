package org.samo_lego.taterzens.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayInteractionManagerMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "processBlockBreakingAction(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/network/packet/c2s/play/PlayerActionC2SPacket$Action;Lnet/minecraft/util/math/Direction;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onAttackBlock(BlockPos blockPos, PlayerActionC2SPacket.Action playerAction, Direction direction, int i, CallbackInfo ci) {
        if (playerAction == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            TaterzenEditor player = (TaterzenEditor) this.player;
            if(player.getNpc() != null && player.inPathEditMode()) {
                player.getNpc().addPathTarget(blockPos);
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new BlockUpdateS2CPacket(blockPos, Blocks.REDSTONE_BLOCK.getDefaultState()));
                ci.cancel();
            }
        }
    }
}
