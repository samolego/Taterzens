package org.samo_lego.taterzens.api.professions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import static org.samo_lego.taterzens.Taterzens.MODID;

public class DefaultProfession implements TaterzenProfession {
    public static final Identifier ID = new Identifier(MODID, "default_profession");
    protected TaterzenNPC npc;

    public DefaultProfession() {
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

    /**
     * Method used for creating the new profession for given taterzen.
     *
     * @param taterzen taterzen to create profession for
     * @return new profession object of taterzen.
     */
    @Override
    public TaterzenProfession create(TaterzenNPC taterzen) {
        DefaultProfession profession = new DefaultProfession();
        profession.npc = taterzen;
        return profession;
    }

    /**
     * Called when Taterzen has the ability to pickup an item.
     * You can create a local inventory in the profession and save it there.
     * Taterzen needs to have {@link TaterzenNPC#canPickUpLoot()} enabled.
     *
     * @param stack stack to be picked up
     *
     * @return true if item should be picked up, otherwise false.
     */
    @Override
    public boolean tryPickupItem(ItemStack stack) {
        return false;
    }
}
