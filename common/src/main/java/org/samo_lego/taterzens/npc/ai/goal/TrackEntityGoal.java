package org.samo_lego.taterzens.npc.ai.goal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

public class TrackEntityGoal extends Goal {
    private final Class<? extends LivingEntity> trackingClass;
    private final PathAwareEntity mob;
    private final TargetPredicate targetPredicate;
    private double x;
    private double y;
    private double z;
    private final double distance;
    private Entity trackingEntity;

    public TrackEntityGoal(PathAwareEntity mob, Class<? extends LivingEntity> targetClass, Predicate<LivingEntity> targetPredicate) {
        super();
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
        this.targetPredicate = new TargetPredicate().setBaseMaxDistance(32.0D).setPredicate(targetPredicate);
        this.trackingClass = targetClass;
        this.distance = 32.0D;
    }

    public boolean canStart() {
        /*if(this.mob.isInWalkTargetRange()) {
            return false;
        } else {*/
            if(this.trackingEntity == null)
                this.trackingEntity = this.findClosestTarget();
        System.out.println(this.trackingEntity);
        if(this.trackingEntity == null)
                return false;

            Vec3d vec3d = TargetFinder.findTargetTowards(this.mob, 16, 7, this.trackingEntity.getPos());
            System.out.println("Found: " + this.trackingEntity);
            if (vec3d == null) {
                return false;
            } else {
                this.x = vec3d.x;
                this.y = vec3d.y;
                this.z = vec3d.z;
                return true;
            }
        //}
    }

    @Nullable
    private Entity findClosestTarget() {
        Entity tracking;
        if (this.trackingClass != PlayerEntity.class && this.trackingClass != ServerPlayerEntity.class) {
            tracking = this.mob.world.getClosestEntity(this.trackingClass, this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.getSearchBox());
        } else {
            tracking = this.mob.world.getClosestPlayer(this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }
        return tracking;
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
