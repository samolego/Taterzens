package org.samo_lego.taterzens.npc.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jetbrains.annotations.Nullable;

/**
 * RevengeGoal but excludes targets that are on same team.
 */
public class TeamRevengeGoal extends HurtByTargetGoal {
    public TeamRevengeGoal(PathfinderMob mob, Class<?>... noRevengeTypes) {
        super(mob, noRevengeTypes);
    }

    @Override
    public boolean canUse() {
        LivingEntity attacker = this.mob.getLastHurtByMob();
        return attacker != null && !attacker.isAlliedTo(this.mob) && super.canUse();
    }

    @Override
    protected boolean canAttack(@Nullable LivingEntity target, TargetingConditions targetPredicate) {
        return target != null && !target.isAlliedTo(this.mob) && super.canAttack(target, targetPredicate);
    }
}
