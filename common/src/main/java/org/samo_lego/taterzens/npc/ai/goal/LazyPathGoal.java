package org.samo_lego.taterzens.npc.ai.goal;


import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;

/**
 * Goal used in {@link org.samo_lego.taterzens.npc.NPCData.Movement#PATH} movement.
 */
public class LazyPathGoal extends DirectPathGoal {

    private final PathfinderMob mob;
    private int nextStartTick;

    public LazyPathGoal(PathfinderMob mob, double speed) {
        super(mob, speed);
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (this.nextStartTick > 0) {
            --this.nextStartTick;
            return false;
        }
        this.nextStartTick = this.nextStartTick(this.mob);
        return true;
    }
    private int nextStartTick(PathfinderMob mob) {
        return MoveToBlockGoal.reducedTickDelay(200 + mob.getRandom().nextInt(200));
    }
}
