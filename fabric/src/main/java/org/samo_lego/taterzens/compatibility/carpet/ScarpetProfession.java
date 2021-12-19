package org.samo_lego.taterzens.compatibility.carpet;

import carpet.script.value.StringValue;
import carpet.script.value.Value;
import carpet.script.value.ValueConversions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.taterzens.api.professions.AbstractProfession;
import org.samo_lego.taterzens.npc.NPCData;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.HashSet;
import java.util.List;

import static org.samo_lego.taterzens.Taterzens.MODID;

public class ScarpetProfession extends AbstractProfession {
    private static final TaterzenScarpetEvent PICKUP_EVENT = new TaterzenScarpetEvent("taterzen_tries_pickup", 3);
    private static final TaterzenScarpetEvent INTERACTION_EVENT = new TaterzenScarpetEvent("taterzen_interacted", 5);
    private static final TaterzenScarpetEvent BEING_ATTACKED_EVENT = new TaterzenScarpetEvent("taterzen_is_attacked", 3);
    private static final TaterzenScarpetEvent TICK_MOVEMENT_EVENT = new TaterzenScarpetEvent("taterzen_movement_ticks", 2);
    private static final TaterzenScarpetEvent REMOVED_EVENT = new TaterzenScarpetEvent("taterzen_removed", 2);
    private static final TaterzenScarpetEvent READ_NBT_EVENT = new TaterzenScarpetEvent("taterzen_nbt_loaded", 3);
    private static final TaterzenScarpetEvent SAVE_NBT_EVENT = new TaterzenScarpetEvent("taterzen_nbt_saved", 3);
    private static final TaterzenScarpetEvent MOVEMENT_SET_EVENT = new TaterzenScarpetEvent("taterzen_movement_set", 3);
    private static final TaterzenScarpetEvent BEHAVIOUR_SET_EVENT = new TaterzenScarpetEvent("taterzen_behaviour_set", 3);
    private static final TaterzenScarpetEvent TRY_RANGED_ATTACK_EVENT = new TaterzenScarpetEvent("taterzen_tries_ranged_attack", 3);
    private static final TaterzenScarpetEvent TRY_MELEE_ATTACK_EVENT = new TaterzenScarpetEvent("taterzen_tries_melee_attack", 3);
    private static final TaterzenScarpetEvent PLAYERS_NEARBY_EVENT = new TaterzenScarpetEvent("taterzen_approached_by", 3);

    private final HashSet<Value> SCARPET_TRAITS = new HashSet<>();
    public static final ResourceLocation ID = new ResourceLocation(MODID, "scarpet_profession");

    public ScarpetProfession(TaterzenNPC npc) {
        super(npc);
    }

    /**
     * Adds a string profession to the taterzen that can be used (mainly) in scarpet.
     * @param scarpetTrait scarpet profession that should be added to taterzen.
     */
    public void addTrait(String scarpetTrait) {
        this.SCARPET_TRAITS.add(StringValue.of(scarpetTrait));
    }

    /**
     * Tries to remove the scarpet profession from taterzen.
     * @param scarpetTrait profession to remove.
     * @return true if removal was successful, otherwise false.
     */
    public boolean removeTrait(String scarpetTrait) {
        return this.SCARPET_TRAITS.remove(StringValue.of(scarpetTrait));
    }

    /**
     * Gets the set of scarpet professions.
     * @return set of scarpet professions that are linked to taterzen.
     */
    public HashSet<Value> getTraits() {
        return this.SCARPET_TRAITS;
    }
    @Override
    public boolean tryPickupItem(ItemEntity itemEntity) {
        PICKUP_EVENT.triggerCustomEvent(this.npc, this.getTraits(), itemEntity);
        return itemEntity.getItem().isEmpty() || itemEntity.isRemoved();
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 pos, InteractionHand hand) {
        INTERACTION_EVENT.triggerCustomEvent(this.npc, this.getTraits(), player, ValueConversions.of(pos), hand);

        return super.interactAt(player, pos, hand);
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        BEING_ATTACKED_EVENT.triggerCustomEvent(this.npc, this.getTraits(), attacker);
        return super.handleAttack(attacker);
    }

    @Override
    public void onPlayersNearby(List<ServerPlayer> players) {
        PLAYERS_NEARBY_EVENT.triggerCustomEvent(this.npc, this.getTraits(), players);
    }

    @Override
    public InteractionResult tickMovement() {
        TICK_MOVEMENT_EVENT.triggerCustomEvent(this.npc, this.getTraits());
        return super.tickMovement();
    }

    @Override
    public void onRemove() {
        REMOVED_EVENT.triggerCustomEvent(this.npc, this.getTraits());
    }

    @Override
    public void readNbt(CompoundTag tag) {
        READ_NBT_EVENT.triggerCustomEvent(this.npc, this.getTraits(), tag);

        ListTag scarpetTraits = (ListTag) tag.get("ScarpetTraits");
        if (scarpetTraits != null) {
            scarpetTraits.forEach(profession -> this.addTrait(profession.getAsString()));
        }
    }

    @Override
    public void saveNbt(CompoundTag tag) {
        SAVE_NBT_EVENT.triggerCustomEvent(this.npc, this.getTraits(), tag);

        if (!this.SCARPET_TRAITS.isEmpty()) {
            ListTag scarpetTraits = new ListTag();
            this.SCARPET_TRAITS.forEach(prof -> scarpetTraits.add(StringTag.valueOf(prof.getPrettyString())));
            tag.put("ScarpetTraits", scarpetTraits);
        }
    }

    @Override
    public void onMovementSet(NPCData.Movement movement) {
        MOVEMENT_SET_EVENT.triggerCustomEvent(this.npc, this.getTraits(), movement);
    }

    @Override
    public void onBehaviourSet(NPCData.Behaviour behaviourLevel) {
        BEHAVIOUR_SET_EVENT.triggerCustomEvent(this.npc, this.getTraits(), behaviourLevel);
    }

    @Override
    public boolean cancelRangedAttack(LivingEntity target) {
        TRY_RANGED_ATTACK_EVENT.triggerCustomEvent(this.npc, this.getTraits(), target);
        return super.cancelRangedAttack(target);
    }

    @Override
    public boolean cancelMeleeAttack(Entity target) {
        TRY_MELEE_ATTACK_EVENT.triggerCustomEvent(this.npc, this.getTraits(), target);
        return super.cancelMeleeAttack(target);
    }
}
