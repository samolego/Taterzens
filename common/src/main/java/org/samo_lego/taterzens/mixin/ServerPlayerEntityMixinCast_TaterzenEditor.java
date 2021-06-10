package org.samo_lego.taterzens.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.successText;

/**
 * Additional methods for players to track {@link TaterzenNPC}
 */
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixinCast_TaterzenEditor implements TaterzenEditor {

    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @Unique
    private TaterzenNPC taterzens$selectedNpc;
    @Unique
    private int taterzens$selectedMsgId = -1; // -1 as no selected msg to edit

    @Unique
    private byte taterzens$lastRenderTick;
    @Unique
    private TaterzenEditor.Types taterzens$editorMode = TaterzenEditor.Types.NONE;

    /**
     * Used for showing the path particles.
     */
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        TaterzenEditor editor = (TaterzenEditor) this.player;
        if(editor.getNpc() != null && taterzens$lastRenderTick++ > 4) {
            if(this.taterzens$editorMode == TaterzenEditor.Types.PATH) {
                ArrayList<BlockPos> pathTargets = editor.getNpc().getPathTargets();
                DustParticleEffect effect = new DustParticleEffect(
                        new Vec3f(
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

                    double distance = Math.sqrt(pos.getSquaredDistance(nextPos));
                    for(double j = 0; j < distance; j += 0.5D) {
                        double x = pos.getX() - j / distance * deltaX;
                        double y = pos.getY() - j / distance * deltaY;
                        double z = pos.getZ() - j / distance * deltaZ;
                        ParticleS2CPacket packet = new ParticleS2CPacket(effect, true, x + 0.5D, y + 1.5D, z + 0.5D, 0.1F, 0.1F, 0.1F, 1.0F, 1);
                        this.player.networkHandler.sendPacket(packet);
                    }
                }
            }
            if(this.taterzens$editorMode != TaterzenEditor.Types.NONE) {
                player.sendMessage(successText("taterzens.tooltip.current_editor", String.valueOf(this.taterzens$editorMode)), true);
            }

            this.taterzens$lastRenderTick = 0;
        }
    }

    @Override
    public void setEditorMode(TaterzenEditor.Types mode) {
        TaterzenEditor editor = (TaterzenEditor) this.player;
        if(editor.getNpc() != null) {
            World world = player.getEntityWorld();
            if(this.taterzens$editorMode == Types.PATH && mode != Types.PATH) {
                editor.getNpc().getPathTargets().forEach(blockPos -> player.networkHandler.sendPacket(
                        new BlockUpdateS2CPacket(blockPos, world.getBlockState(blockPos))
                ));
            } else if(this.taterzens$editorMode != Types.PATH && mode == Types.PATH) {
                editor.getNpc().getPathTargets().forEach(blockPos -> player.networkHandler.sendPacket(
                        new BlockUpdateS2CPacket(blockPos, Blocks.REDSTONE_BLOCK.getDefaultState())
                ));
            }
        }

        this.taterzens$editorMode = mode;
    }

    @Override
    public Types getEditorMode() {
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
