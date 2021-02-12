package org.samo_lego.taterzens.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
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

/**
 * Additional methods for players to track {@link TaterzenNPC}
 */
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixinCast_TaterzenEditor implements TaterzenEditor {
    @Unique
    private TaterzenNPC selectedNpc;
    @Unique
    private boolean inEditMode;
    @Unique
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
    @Unique
    private byte lastRenderTick = 0;

    /**
     * Gets the selected {@link TaterzenNPC} if player has it.
     * @return TaterzenNPC if player has one selected, otherwise null.
     */
    @Nullable
    @Override
    public TaterzenNPC getNpc() {
        return this.selectedNpc;
    }

    @Override
    public void selectNpc(@Nullable TaterzenNPC npc) {
        this.selectedNpc = npc;
    }

    @Override
    public boolean inPathEditMode() {
        return this.inEditMode;
    }

    /**
     * Sets whether player is in path edit mode.
     * If true, any blocks broken will be added to Taterzen's "goals"
     * for {@link org.samo_lego.taterzens.npc.NPCData.Movement} types {@link org.samo_lego.taterzens.npc.NPCData.Movement#PATH}
     * and {@link org.samo_lego.taterzens.npc.NPCData.Movement#FORCED_PATH}.
     *
     * @param editMode whether player should enter path edit mode.
     */
    @Override
    public void setPathEditMode(boolean editMode) {
        this.inEditMode = editMode;
        if(selectedNpc != null) {
            World world = player.getEntityWorld();
            selectedNpc.getPathTargets().forEach(blockPos -> player.networkHandler.sendPacket(
                    new BlockUpdateS2CPacket(blockPos, editMode ? Blocks.REDSTONE_BLOCK.getDefaultState() : world.getBlockState(blockPos))
            ));
        }
    }


    /**
     * Used for showing the path particles.
     * @param ci
     */
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if(this.inEditMode && selectedNpc != null && lastRenderTick++ > 4) {
            ArrayList<BlockPos> pathTargets = this.selectedNpc.getPathTargets();
            DustParticleEffect effect = new DustParticleEffect(
                    config.path.color.red / 255.0F,
                    config.path.color.green / 255.0F,
                    config.path.color.blue / 255.0F, 1.0F
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
                this.lastRenderTick = 0;
            }
        }
    }
}
