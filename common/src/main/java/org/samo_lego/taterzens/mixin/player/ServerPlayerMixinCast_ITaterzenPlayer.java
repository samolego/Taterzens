package org.samo_lego.taterzens.mixin.player;

import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.taterzens.interfaces.ITaterzenPlayer;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.UUID;

import static org.samo_lego.taterzens.Taterzens.config;

/**
 * Additional methods for players to track {@link TaterzenNPC}
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixinCast_ITaterzenPlayer implements ITaterzenPlayer {

    /**
     * Stores last interaction time.
     * Used to prevent double interaction on single click
     * if fake type of {@link TaterzenNPC} is {@link net.minecraft.world.entity.EntityType#PLAYER}
     * or {@link net.minecraft.world.entity.EntityType#ARMOR_STAND}.
     */
    @Unique
    private long taterzens$lastNPCInteraction = 0;
    /**
     * Ticks since this player got last message from taterzen.
     */
    @Unique
    private final HashMap<UUID, Integer> taterzens$lastMessageTicks = new HashMap<>();
    /**
     * The last message index of the message that was sent
     * to player.
     */
    @Unique
    private final HashMap<UUID, Integer> taterzens$currentMsg = new HashMap<>();

    @Override
    public long getLastInteractionTime() {
        return taterzens$lastNPCInteraction;
    }

    @Override
    public void setLastInteraction(long time) {
        this.taterzens$lastNPCInteraction = time;
    }

    @Override
    public int ticksSinceLastMessage(UUID taterzenUuid) {
        if(!this.taterzens$lastMessageTicks.containsKey(taterzenUuid))
            this.taterzens$lastMessageTicks.put(taterzenUuid, config.messages.messageDelay + 1);
        return this.taterzens$lastMessageTicks.get(taterzenUuid);
    }

    @Override
    public void resetMessageTicks(UUID taterzenUuid) {
        this.taterzens$lastMessageTicks.put(taterzenUuid, 0);
    }

    @Override
    public int getLastMsgPos(UUID taterzenUuid) {
        if(!this.taterzens$currentMsg.containsKey(taterzenUuid))
            this.taterzens$currentMsg.put(taterzenUuid, 0);
        return this.taterzens$currentMsg.get(taterzenUuid);
    }

    @Override
    public void setLastMsgPos(UUID taterzenUuid, int newPos) {
        this.taterzens$currentMsg.put(taterzenUuid, newPos);
    }

    /**
     * Increases the ticks since last message counter.
     */
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void postTick(CallbackInfo ci) {
        for(UUID npcId : this.taterzens$lastMessageTicks.keySet()) {
            int ticks = this.taterzens$lastMessageTicks.get(npcId) + 1;
            this.taterzens$lastMessageTicks.put(npcId, ticks);
        }
    }
}
