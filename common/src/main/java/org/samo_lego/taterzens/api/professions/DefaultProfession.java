package org.samo_lego.taterzens.api.professions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.taterzens.npc.TaterzenNPC;

public class DefaultProfession implements TaterzenProfession {
    public static final String TYPE = "default";
    private final TaterzenNPC npc;

    public DefaultProfession(TaterzenNPC taterzen) {
        this.npc = taterzen;
    }

    @Override
    public boolean tick() {
        return false;
    }

    @Override
    public boolean tickMovement() {
        return false;
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d pos, Hand hand) {
        return ActionResult.PASS;
    }

    /**
     * Called when Taterzen is attack
     *
     * @param attacker entity that is attacking
     *
     * @return true to cancel the attack, otherwise false
     */
    @Override
    public boolean handleAttack(Entity attacker) {
        return false;
    }

    /**
     * Called onb Taterzen detah / removal.
     */
    @Override
    public void onRemove() {

    }

    @Override
    public void fromTag(CompoundTag tag) {

    }

    @Override
    public void toTag(CompoundTag tag) {

    }
}
