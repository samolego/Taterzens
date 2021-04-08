package org.samo_lego.taterzens.npc.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ReachMeleeAttackGoal extends MeleeAttackGoal {

    private final PathAwareEntity mob;

    public ReachMeleeAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
        super(mob, speed, pauseWhenMobIdle);
        this.mob = mob;
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity livingEntity = this.mob.getTarget();
        if(livingEntity == null || !livingEntity.isAlive()) {
            return false;
        }
        return !(livingEntity instanceof PlayerEntity) || (!livingEntity.isSpectator() && !((PlayerEntity) livingEntity).isCreative());
    }

    @Override
    protected double getSquaredMaxAttackDistance(LivingEntity entity) {
        return 12.25D;
    }
}
