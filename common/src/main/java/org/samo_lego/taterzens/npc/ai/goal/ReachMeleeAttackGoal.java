package org.samo_lego.taterzens.npc.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;

public class ReachMeleeAttackGoal extends MeleeAttackGoal {

    private final PathfinderMob mob;

    public ReachMeleeAttackGoal(PathfinderMob mob, double speed, boolean pauseWhenMobIdle) {
        super(mob, speed, pauseWhenMobIdle);
        this.mob = mob;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if(livingEntity == null || !livingEntity.isAlive()) {
            return false;
        }
        return !(livingEntity instanceof Player) || (!livingEntity.isSpectator() && !((Player) livingEntity).isCreative());
    }

    @Override
    protected double getAttackReachSqr(LivingEntity entity) {
        return 12.25D;
    }
}
