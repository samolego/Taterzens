package org.samo_lego.taterzens.npc.ai.goal;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TrackUuidGoal extends Goal {
    private final PathfinderMob mob;
    private double x;
    private double y;
    private double z;
    private final double distance;
    private Entity trackingEntity;
    private final Predicate<Entity> trackingUuid;

    public TrackUuidGoal(PathfinderMob mob, Predicate<Entity> targetPredicate) {
        super();
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.distance = 32.0D;
        this.trackingUuid = targetPredicate;
    }

    public boolean canUse() {
        if(this.mob.isPathFinding() || (this.trackingEntity != null && this.trackingEntity.isAlive() && this.mob.distanceToSqr(this.trackingEntity) < this.distance)) {
            return false;
        } else {
            if(this.trackingEntity == null || !this.trackingEntity.isAlive())
                this.findClosestTarget();
            if(this.trackingEntity == null)
                return false;

            Vec3 vec3d = this.trackingEntity.position();
            this.x = vec3d.x;
            this.y = vec3d.y;
            this.z = vec3d.z;
            return true;
        }
    }

    private void findClosestTarget() {
        List<Entity> entities = this.mob.level.getEntitiesOfClass(Entity.class, this.getSearchBox(), this.trackingUuid);
        this.trackingEntity = entities.isEmpty() ? null : entities.get(0);
    }


    private AABB getSearchBox() {
        return this.mob.getBoundingBox().inflate(this.distance, 4.0D, this.distance);
    }

    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone();
    }

    public void start() {
        this.mob.getNavigation().moveTo(this.x, this.y, this.z, 1.0F);
    }
}
