package org.samo_lego.taterzens.mixin.player;

import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin_PeacefulDamage extends LivingEntity {

    private final Player player = (Player) (Object) this;

    protected PlayerMixin_PeacefulDamage(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "hurt",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;removeEntitiesOnShoulder()V",
                    shift = At.Shift.AFTER),
            cancellable = true
    )
    private void enableTaterzenPeacefulDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = source.getEntity();
        if (attacker instanceof TaterzenNPC && this.player.getLevel().getDifficulty() == Difficulty.PEACEFUL) {
            // Vanilla cancels damage if the world is in peaceful mode
            cir.setReturnValue(amount == 0.0f || super.hurt(source, amount));
        }
    }

}
