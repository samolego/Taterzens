package org.samo_lego.taterzens.npc.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.world.entity.ai.targeting.TargetingConditions.forNonCombat;

public class TrackEntityGoal extends Goal {
    private final Class<? extends LivingEntity> trackingClass;
    private final PathfinderMob mob;
    private final TargetingConditions targetPredicate;
    private double x;
    private double y;
    private double z;
    private final double distance;
    private Entity trackingEntity;

    public TrackEntityGoal(PathfinderMob mob, Class<? extends LivingEntity> targetClass, Predicate<LivingEntity> targetPredicate) {
        super();
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.targetPredicate = forNonCombat().range(32.0D).selector(targetPredicate);
        this.trackingClass = targetClass;
        this.distance = 32.0D;
    }

    public boolean canUse() {
        if((this.trackingEntity != null && this.trackingEntity.isAlive() && this.mob.distanceToSqr(this.trackingEntity) < this.distance) ||  this.mob.isPathFinding()) {
            return false;
        } else {
            if(this.trackingEntity == null || !this.trackingEntity.isAlive() || this.mob.distanceTo(this.trackingEntity) > this.distance)
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

    public void resetTrackingEntity() {
        this.trackingEntity = null;
    }

    private void findClosestTarget() {
        if (this.trackingClass != Player.class && this.trackingClass != ServerPlayer.class) {
            this.trackingEntity = this.mob.level.getNearestEntity(this.trackingClass, this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.getSearchBox());
        } else {
            this.trackingEntity = this.mob.level.getNearestPlayer(this.targetPredicate, this.mob);
        }
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
