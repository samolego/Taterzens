package org.samo_lego.taterzens.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.taterzens.interfaces.TaterzenPlayer;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    private long taterzens$lastNPCInteraction = 0;
    /**
     * Ticks since this player got last message from taterzen.
     */
    @Unique
    private int taterzens$lastMessageTicks = 0;
    /**
     * The last message index of the message that was sent
     * to player.
     */
    @Unique
    private int taterzens$currentMsg = 0;

    @Override
    public long getLastInteractionTime() {
        return taterzens$lastNPCInteraction;
    }

    @Override
    public void setLastInteraction(long time) {
        this.taterzens$lastNPCInteraction = time;
    }

    @Override
    public int ticksSinceLastMessage() {
        return this.taterzens$lastMessageTicks;
    }

    @Override
    public void resetMessageTicks() {
        this.taterzens$lastMessageTicks = 0;
    }

    @Override
    public int getLastMsgPos() {
        return this.taterzens$currentMsg;
    }

    @Override
    public void setLastMsgPos(int newPos) {
        this.taterzens$currentMsg = newPos;
    }

    /**
     * Increases the ticks since last message counter.
     * @param ci
     */
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void postTick(CallbackInfo ci) {
        ++this.taterzens$lastMessageTicks;
    }
}
