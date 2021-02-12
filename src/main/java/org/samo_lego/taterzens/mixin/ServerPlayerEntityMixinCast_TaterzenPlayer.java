package org.samo_lego.taterzens.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.taterzens.interfaces.TaterzenPlayer;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Additional methods for players to track {@link TaterzenNPC}
 */
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixinCast_TaterzenPlayer implements TaterzenPlayer {

    /**
     * Stores last interaction time.
     * Used to prevent double interaction on single click
     * if fake type of {@link TaterzenNPC} is {@link net.minecraft.entity.EntityType#PLAYER}
     * or {@link net.minecraft.entity.EntityType#ARMOR_STAND}.
     */
    @Unique
    private long lastNPCInteraction = 0;

    @Override
    public long getLastInteractionTime() {
        return lastNPCInteraction;
    }

    @Override
    public void setLastInteraction(long time) {
        this.lastNPCInteraction = time;
    }
}
