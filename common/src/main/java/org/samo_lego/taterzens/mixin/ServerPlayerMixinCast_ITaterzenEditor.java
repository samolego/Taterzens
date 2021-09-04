package org.samo_lego.taterzens.mixin;

import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.successText;

import com.mojang.math.Vector3f;

/**
 * Additional methods for players to track {@link TaterzenNPC}
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerMixinCast_ITaterzenEditor implements ITaterzenEditor {

    private final ServerPlayer player = (ServerPlayer) (Object) this;

    @Unique
    private TaterzenNPC taterzens$selectedNpc;
    @Unique
    private int taterzens$selectedMsgId = -1; // -1 as no selected msg to edit

    @Unique
    private byte taterzens$lastRenderTick;
    @Unique
    private EditorMode taterzens$editorMode = EditorMode.NONE;

    /**
     * Used for showing the path particles.
     */
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        ITaterzenEditor editor = (ITaterzenEditor) this.player;
        if(editor.getNpc() != null && taterzens$lastRenderTick++ > 4) {
            if(this.taterzens$editorMode == EditorMode.PATH) {
                ArrayList<BlockPos> pathTargets = editor.getNpc().getPathTargets();
                DustParticleOptions effect = new DustParticleOptions(
                        new Vector3f(
                            config.path.color.red / 255.0F,
                            config.path.color.green / 255.0F,
                            config.path.color.blue / 255.0F
                        ),
                        1.0F
                );

                for(int i = 0; i < pathTargets.size(); ++i) {
                    BlockPos pos = pathTargets.get(i);
                    BlockPos nextPos = pathTargets.get(i +1 == pathTargets.size() ? 0 : i + 1);

                    int deltaX = pos.getX() - nextPos.getX();
                    int deltaY = pos.getY() - nextPos.getY();
                    int deltaZ = pos.getZ() - nextPos.getZ();

                    double distance = Math.sqrt(pos.distSqr(nextPos));
                    for(double j = 0; j < distance; j += 0.5D) {
                        double x = pos.getX() - j / distance * deltaX;
                        double y = pos.getY() - j / distance * deltaY;
                        double z = pos.getZ() - j / distance * deltaZ;
                        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(effect, true, x + 0.5D, y + 1.5D, z + 0.5D, 0.1F, 0.1F, 0.1F, 1.0F, 1);
                        this.player.connection.send(packet);
                    }
                }
            }
            if(this.taterzens$editorMode != EditorMode.NONE) {
                player.displayClientMessage(successText("taterzens.tooltip.current_editor", String.valueOf(this.taterzens$editorMode)), true);
            }

            this.taterzens$lastRenderTick = 0;
        }
    }

    @Override
    public void setEditorMode(EditorMode mode) {
        ITaterzenEditor editor = (ITaterzenEditor) this.player;
        if(editor.getNpc() != null) {
            Level world = player.getLevel();
            if(this.taterzens$editorMode == EditorMode.PATH && mode != EditorMode.PATH) {
                editor.getNpc().getPathTargets().forEach(blockPos -> player.connection.send(
                        new ClientboundBlockUpdatePacket(blockPos, world.getBlockState(blockPos))
                ));
            } else if(this.taterzens$editorMode != EditorMode.PATH && mode == EditorMode.PATH) {
                editor.getNpc().getPathTargets().forEach(blockPos -> player.connection.send(
                        new ClientboundBlockUpdatePacket(blockPos, Blocks.REDSTONE_BLOCK.defaultBlockState())
                ));
            }
        }

        this.taterzens$editorMode = mode;
    }

    @Override
    public EditorMode getEditorMode() {
        return this.taterzens$editorMode;
    }

    /**
     * Gets the selected {@link TaterzenNPC} if player has it.
     * @return TaterzenNPC if player has one selected, otherwise null.
     */
    @Nullable
    @Override
    public TaterzenNPC getNpc() {
        return this.taterzens$selectedNpc;
    }

    @Override
    public void selectNpc(@Nullable TaterzenNPC npc) {
        this.taterzens$selectedNpc = npc;
    }

    @Override
    public void setEditingMessageIndex(int selected) {
        this.taterzens$selectedMsgId = selected;
    }

    @Override
    public int getEditingMessageIndex() {
        return this.taterzens$selectedMsgId;
    }
}
