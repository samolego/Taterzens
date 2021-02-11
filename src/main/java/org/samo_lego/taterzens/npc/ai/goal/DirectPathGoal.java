package org.samo_lego.taterzens.npc.ai.goal;


import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public class DirectPathGoal extends GoToWalkTargetGoal {

    private final PathAwareEntity mob;
    private final double speed;

    public DirectPathGoal(PathAwareEntity mob, double speed) {
        super(mob, speed);
        this.mob = mob;
        this.speed = speed;
    }

    @Override
    public boolean canStart() {
        return !this.mob.isInWalkTargetRange();
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingTo(
                this.mob.getPositionTarget().getX(),
                this.mob.getPositionTarget().getY(),
                this.mob.getPositionTarget().getZ(),
                this.speed
        );
    }
}
