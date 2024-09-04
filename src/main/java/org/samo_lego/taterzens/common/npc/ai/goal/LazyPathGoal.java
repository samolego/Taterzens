package org.samo_lego.taterzens.common.npc.ai.goal;


import net.minecraft.world.entity.PathfinderMob;
import org.samo_lego.taterzens.common.npc.NPCData;

/**
 * Goal used in {@link NPCData.Movement#PATH} movement.
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
        return reducedTickDelay(200 + mob.getRandom().nextInt(200));
    }
}
