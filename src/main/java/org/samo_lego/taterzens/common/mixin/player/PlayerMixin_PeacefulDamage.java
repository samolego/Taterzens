package org.samo_lego.taterzens.common.mixin.player;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.samo_lego.taterzens.common.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin_PeacefulDamage extends LivingEntity {

    @Unique
    private final Player self = (Player) (Object) this;

    protected PlayerMixin_PeacefulDamage(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "hurtServer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;removeEntitiesOnShoulder()V",
                    shift = At.Shift.AFTER),
            cancellable = true
    )
    private void enableTaterzenPeacefulDamage(ServerLevel serverLevel, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = source.getEntity();
        if (attacker instanceof TaterzenNPC && this.self.level().getDifficulty() == Difficulty.PEACEFUL) {
            // Vanilla cancels damage if the world is in peaceful mode
            cir.setReturnValue(amount == 0.0f || super.hurtServer(serverLevel, source, amount));
        }
    }

}
