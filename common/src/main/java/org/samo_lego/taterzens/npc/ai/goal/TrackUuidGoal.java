package org.samo_lego.taterzens.npc.ai.goal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class TrackUuidGoal extends Goal {
    private final PathAwareEntity mob;
    private double x;
    private double y;
    private double z;
    private final double distance;
    private Entity trackingEntity;
    private final Predicate<Entity> trackingUuid;

    public TrackUuidGoal(PathAwareEntity mob, Predicate<Entity> targetPredicate) {
        super();
        this.mob = mob;
        this.setControls(EnumSet.of(Control.MOVE));
        this.distance = 32.0D;
        this.trackingUuid = targetPredicate;
    }

    public boolean canStart() {
        if(this.mob.isNavigating() || (this.trackingEntity != null && this.trackingEntity.isAlive() && this.mob.squaredDistanceTo(this.trackingEntity) < this.distance)) {
            return false;
        } else {
            if(this.trackingEntity == null || !this.trackingEntity.isAlive())
                this.findClosestTarget();
            if(this.trackingEntity == null)
                return false;

            Vec3d vec3d = this.trackingEntity.getPos();
            this.x = vec3d.x;
            this.y = vec3d.y;
            this.z = vec3d.z;
            return true;
        }
    }

    private void findClosestTarget() {
        List<Entity> entities = this.mob.world.getEntitiesByClass(Entity.class, this.getSearchBox(), this.trackingUuid);
        this.trackingEntity = entities.isEmpty() ? null : entities.get(0);
    }


    private Box getSearchBox() {
        return this.mob.getBoundingBox().expand(this.distance, 4.0D, this.distance);
    }

    public boolean shouldContinue() {
        return !this.mob.getNavigation().isIdle();
    }

    public void start() {
        this.mob.getNavigation().startMovingTo(this.x, this.y, this.z, 1.0F);
    }
}
