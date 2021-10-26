package org.samo_lego.taterzens.compatibility.carpet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.taterzens.api.professions.TaterzenProfession;
import org.samo_lego.taterzens.npc.NPCData;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.List;

public class ScarpetProfession implements TaterzenProfession {
    private TaterzenNPC taterzen;
    private final ResourceLocation professionId;
    private static final TaterzenScarpetEvent PICKUP_EVENT = new TaterzenScarpetEvent("taterzen_tries_pickup", 2);
    private static final TaterzenScarpetEvent INTERACTION_EVENT = new TaterzenScarpetEvent("taterzen_interacted", 4);
    private static final TaterzenScarpetEvent BEING_ATTACKED_EVENT = new TaterzenScarpetEvent("taterzen_is_attacked", 2);
    private static final TaterzenScarpetEvent TICK_MOVEMENT_EVENT = new TaterzenScarpetEvent("taterzen_movement_ticks", 1);
    private static final TaterzenScarpetEvent REMOVED_EVENT = new TaterzenScarpetEvent("taterzen_removed", 1);
    private static final TaterzenScarpetEvent READ_NBT_EVENT = new TaterzenScarpetEvent("taterzen_nbt_loaded", 2);
    private static final TaterzenScarpetEvent SAVE_NBT_EVENT = new TaterzenScarpetEvent("taterzen_nbt_saved", 2);
    private static final TaterzenScarpetEvent MOVEMENT_SET_EVENT = new TaterzenScarpetEvent("taterzen_movement_set", 2);
    private static final TaterzenScarpetEvent BEHAVIOUR_SET_EVENT = new TaterzenScarpetEvent("taterzen_behaviour_set", 2);
    private static final TaterzenScarpetEvent TRY_RANGED_ATTACK_EVENT = new TaterzenScarpetEvent("taterzen_tries_ranged_attack", 2);
    private static final TaterzenScarpetEvent TRY_MELEE_ATTACK_EVENT = new TaterzenScarpetEvent("taterzen_tries_melee_attack", 2);
    private static final TaterzenScarpetEvent PLAYERS_NEARBY_EVENT = new TaterzenScarpetEvent("taterzen_approached_by", 2);

    public ScarpetProfession(ResourceLocation professionId) {
        this.professionId = professionId;
    }

    @Override
    public boolean tryPickupItem(ItemEntity itemEntity) {
        PICKUP_EVENT.triggerCustomEvent(this.taterzen, itemEntity);
        return itemEntity.getItem().isEmpty() || itemEntity.isRemoved();
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 pos, InteractionHand hand) {
        INTERACTION_EVENT.triggerCustomEvent(this.taterzen, player, pos, hand);
        return TaterzenProfession.super.interactAt(player, pos, hand);
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        BEING_ATTACKED_EVENT.triggerCustomEvent(this.taterzen, attacker);
        return TaterzenProfession.super.handleAttack(attacker);
    }

    @Override
    public void onPlayersNearby(List<ServerPlayer> players) {
        PLAYERS_NEARBY_EVENT.triggerCustomEvent(this.taterzen, players);
        TaterzenProfession.super.onPlayersNearby(players);
    }

    @Override
    public InteractionResult tickMovement() {
        TICK_MOVEMENT_EVENT.triggerCustomEvent(this.taterzen);
        return TaterzenProfession.super.tickMovement();
    }

    @Override
    public void onRemove() {
        REMOVED_EVENT.triggerCustomEvent(this.taterzen);
        TaterzenProfession.super.onRemove();
    }

    @Override
    public void readNbt(CompoundTag tag) {
        READ_NBT_EVENT.triggerCustomEvent(this.taterzen, tag);
        TaterzenProfession.super.readNbt(tag);
    }

    @Override
    public void saveNbt(CompoundTag tag) {
        SAVE_NBT_EVENT.triggerCustomEvent(this.taterzen, tag);
        TaterzenProfession.super.saveNbt(tag);
    }

    @Override
    public void onMovementSet(NPCData.Movement movement) {
        MOVEMENT_SET_EVENT.triggerCustomEvent(this.taterzen, movement);
        TaterzenProfession.super.onMovementSet(movement);
    }

    @Override
    public void onBehaviourSet(NPCData.Behaviour behaviourLevel) {
        BEHAVIOUR_SET_EVENT.triggerCustomEvent(this.taterzen, behaviourLevel);
        TaterzenProfession.super.onBehaviourSet(behaviourLevel);
    }

    @Override
    public boolean cancelRangedAttack(LivingEntity target) {
        TRY_RANGED_ATTACK_EVENT.triggerCustomEvent(this.taterzen, target);
        return TaterzenProfession.super.cancelRangedAttack(target);
    }

    @Override
    public boolean cancelMeleeAttack(Entity target) {
        TRY_MELEE_ATTACK_EVENT.triggerCustomEvent(this.taterzen, target);
        return TaterzenProfession.super.cancelMeleeAttack(target);
    }

    @Override
    public TaterzenProfession create(TaterzenNPC taterzen) {
        ScarpetProfession profession = new ScarpetProfession(this.professionId);
        profession.taterzen = taterzen;
        return profession;
    }
}
