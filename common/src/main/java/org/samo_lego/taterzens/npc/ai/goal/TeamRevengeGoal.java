package org.samo_lego.taterzens.npc.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.jetbrains.annotations.Nullable;

/**
 * RevengeGoal but excludes targets that are on same team.
 */
public class TeamRevengeGoal extends RevengeGoal {
    public TeamRevengeGoal(PathAwareEntity mob, Class<?>... noRevengeTypes) {
        super(mob, noRevengeTypes);
    }

    @Override
    public boolean canStart() {
        LivingEntity attacker = this.mob.getAttacker();
        return attacker != null && !attacker.isTeammate(this.mob) && super.canStart();
    }

    @Override
    protected boolean canTrack(@Nullable LivingEntity target, TargetPredicate targetPredicate) {
        return target != null && !target.isTeammate(this.mob) && super.canTrack(target, targetPredicate);
    }
}
