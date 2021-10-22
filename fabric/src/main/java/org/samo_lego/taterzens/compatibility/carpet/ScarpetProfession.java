package org.samo_lego.taterzens.compatibility.carpet;

import carpet.script.CarpetEventServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.taterzens.api.professions.TaterzenProfession;
import org.samo_lego.taterzens.npc.NPCData;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.List;

public class ScarpetProfession implements TaterzenProfession {
    private TaterzenNPC taterzen;
    private final ResourceLocation professionId;
    private static final CarpetEventServer.Event PICKUP_EVENT = new CarpetEventServer.Event("taterzen_tries_pickup", 2, true);
    private static final CarpetEventServer.Event BEING_ATTACKED_EVENT = new CarpetEventServer.Event("taterzen_is_attacked", 2, true);
    private static final CarpetEventServer.Event TICK_MOVEMENT_EVENT = new CarpetEventServer.Event("taterzen_movement_ticks", 1, true);
    private static final CarpetEventServer.Event REMOVED_EVENT = new CarpetEventServer.Event("taterzen_removed", 1, true);
    private static final CarpetEventServer.Event READ_NBT_EVENT = new CarpetEventServer.Event("taterzen_nbt_loaded", 2, true);
    private static final CarpetEventServer.Event SAVE_NBT_EVENT = new CarpetEventServer.Event("taterzen_nbt_saved", 2, true);
    private static final CarpetEventServer.Event MOVEMENT_SET_EVENT = new CarpetEventServer.Event("taterzen_movement_set", 2, true);
    private static final CarpetEventServer.Event BEHAVIOUR_SET_EVENT = new CarpetEventServer.Event("taterzen_behaviour_set", 2, true);
    private static final CarpetEventServer.Event TRY_RANGED_ATTACK_EVENT = new CarpetEventServer.Event("taterzen_tries_ranged_attack", 2, true);
    private static final CarpetEventServer.Event TRY_MELEE_ATTACK_EVENT = new CarpetEventServer.Event("taterzen_tries_melee_attack", 2, true);
    private static final CarpetEventServer.Event PLAYERS_NEARBY_EVENT = new CarpetEventServer.Event("taterzen_approached_by", 2, true);

    public ScarpetProfession(ResourceLocation professionId) {
        this.professionId = professionId;
    }

    @Override
    public boolean tryPickupItem(ItemStack groundStack) {
        PICKUP_EVENT.onCustomWorldEvent((ServerLevel) this.taterzen.level, this.taterzen, groundStack);
        return TaterzenProfession.super.tryPickupItem(groundStack);
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        BEING_ATTACKED_EVENT.onCustomWorldEvent((ServerLevel) this.taterzen.level, this.taterzen, attacker);
        return TaterzenProfession.super.handleAttack(attacker);
    }

    @Override
    public void onPlayersNearby(List<Entity> players) {
        PLAYERS_NEARBY_EVENT.onCustomWorldEvent((ServerLevel) this.taterzen.level, this.taterzen, players);
        TaterzenProfession.super.onPlayersNearby(players);
    }

    @Override
    public InteractionResult tickMovement() {
        TICK_MOVEMENT_EVENT.onCustomWorldEvent((ServerLevel) this.taterzen.level, this.taterzen);
        return TaterzenProfession.super.tickMovement();
    }

    @Override
    public void onRemove() {
        REMOVED_EVENT.onCustomWorldEvent((ServerLevel) this.taterzen.level, this.taterzen);
        TaterzenProfession.super.onRemove();
    }

    @Override
    public void readNbt(CompoundTag tag) {
        READ_NBT_EVENT.onCustomWorldEvent((ServerLevel) this.taterzen.level, this.taterzen, tag);
        TaterzenProfession.super.readNbt(tag);
    }

    @Override
    public void saveNbt(CompoundTag tag) {
        SAVE_NBT_EVENT.onCustomWorldEvent((ServerLevel) this.taterzen.level, this.taterzen, tag);
        TaterzenProfession.super.saveNbt(tag);
    }

    @Override
    public void onMovementSet(NPCData.Movement movement) {
        MOVEMENT_SET_EVENT.onCustomWorldEvent((ServerLevel) this.taterzen.level, this.taterzen, movement);
        TaterzenProfession.super.onMovementSet(movement);
    }

    @Override
    public void onBehaviourSet(NPCData.Behaviour behaviourLevel) {
        BEHAVIOUR_SET_EVENT.onCustomWorldEvent((ServerLevel) this.taterzen.level, this.taterzen, behaviourLevel);
        TaterzenProfession.super.onBehaviourSet(behaviourLevel);
    }

    @Override
    public boolean cancelRangedAttack(LivingEntity target) {
        TRY_RANGED_ATTACK_EVENT.onCustomWorldEvent((ServerLevel) this.taterzen.level, this.taterzen, target);
        return TaterzenProfession.super.cancelRangedAttack(target);
    }

    @Override
    public boolean cancelMeleeAttack(Entity target) {
        TRY_MELEE_ATTACK_EVENT.onCustomWorldEvent((ServerLevel) this.taterzen.level, this.taterzen, target);
        return TaterzenProfession.super.cancelMeleeAttack(target);
    }

    @Override
    public TaterzenProfession create(TaterzenNPC taterzen) {
        ScarpetProfession profession = new ScarpetProfession(this.professionId);
        profession.taterzen = taterzen;
        return profession;
    }
}
