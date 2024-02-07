package org.samo_lego.taterzens.common.npc.ai.goal;


import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import org.samo_lego.taterzens.common.npc.NPCData;

/**
 * Goal used in {@link NPCData.Movement FORCED_PATH} movement.
 */
public class DirectPathGoal extends MoveTowardsRestrictionGoal {

    private final PathfinderMob mob;
    private final double speed;

    public DirectPathGoal(PathfinderMob mob, double speed) {
        super(mob, speed);
        this.mob = mob;
        this.speed = speed;
    }

    @Override
    public boolean canUse() {
        return !this.mob.isWithinRestriction();
    }

    /**
     * Starts moving mob directly to its target.
     */
    @Override
    public void start() {
        this.mob.getNavigation().moveTo(
                this.mob.getRestrictCenter().getX(),
                this.mob.getRestrictCenter().getY(),
                this.mob.getRestrictCenter().getZ(),
                this.speed
        );
    }
}
