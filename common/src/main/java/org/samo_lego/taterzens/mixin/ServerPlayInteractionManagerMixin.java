package org.samo_lego.taterzens.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
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

import static org.samo_lego.taterzens.util.TextUtil.successText;

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
     * @param blockPos position of the broken block
     * @param playerAction action the player is trying to do
     * @param direction direction
     * @param worldHeight world height
     */
    @Inject(
            method = "handleBlockBreakAction(Lnet/minecraft/core/BlockPos;Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/Direction;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAttackBlock(BlockPos blockPos, ServerboundPlayerActionPacket.Action playerAction, Direction direction, int worldHeight, CallbackInfo ci) {
        if (playerAction == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            ITaterzenEditor editorPlayer = (ITaterzenEditor) this.player;
            if(editorPlayer.getNpc() != null && ((ITaterzenEditor) this.player).getEditorMode() == ITaterzenEditor.EditorMode.PATH) {
                editorPlayer.getNpc().addPathTarget(blockPos);
                ServerPlayer serverPlayer = ((ServerPlayer) editorPlayer);
                serverPlayer.connection.send(new ClientboundBlockUpdatePacket(blockPos, Blocks.REDSTONE_BLOCK.defaultBlockState()));
                serverPlayer.sendMessage(successText("taterzens.command.path_editor.add.success", "(" + blockPos.toShortString() + ")"), ChatType.SYSTEM, serverPlayer.getUUID());
                ci.cancel();
            }
        }
    }
}
