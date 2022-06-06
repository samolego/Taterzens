package org.samo_lego.taterzens.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.Blocks;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayInteractionManagerMixin {

    @Final
    @Shadow
    protected ServerPlayer player;

    /**
     * Used for detecting block breaking. Broken blocks count as new nodes
     * for the path of player's {@link org.samo_lego.taterzens.npc.TaterzenNPC}.
     * Activated only if player is in path edit mode and has a selected Taterzen.
     *
     * @param blockPos  position of the broken block
     * @param action    action the player is trying to do
     * @param direction direction
     * @param i
     * @param j
     * @param ci
     */
    @Inject(
            method = "handleBlockBreakAction",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAttackBlock(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i, int j, CallbackInfo ci) {
        if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            ITaterzenEditor player = (ITaterzenEditor) this.player;
            if (player.getNpc() != null && ((ITaterzenEditor) this.player).getEditorMode() == ITaterzenEditor.EditorMode.PATH) {
                player.getNpc().addPathTarget(blockPos);
                ((ServerPlayer) player).connection.send(new ClientboundBlockUpdatePacket(blockPos, Blocks.REDSTONE_BLOCK.defaultBlockState()));
                ci.cancel();
            }
        }
    }
}
