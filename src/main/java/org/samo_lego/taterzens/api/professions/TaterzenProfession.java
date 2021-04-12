package org.samo_lego.taterzens.api.professions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * Profession interface, providing hooks
 * for Taterzen's behaviour.
 *
 * Booleans instead of voids are there to allow you to cancel
 * the base Taterzen method.
 */
public interface TaterzenProfession {
    boolean tick();
    void tickMovement();
    ActionResult interactAt(PlayerEntity player, Vec3d pos, Hand hand);

    /**
     * Called when Taterzen is attack
     * @param attacker entity that is attacking
     * @return true to cancel the attack, otherwise false
     */
    boolean handleAttack(Entity attacker);

    void fromTag(CompoundTag tag);
    void toTag(CompoundTag tag);
}
