package org.samo_lego.taterzens.api.professions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.taterzens.npc.NPCData;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.List;

/**
 * Profession interface, providing hooks
 * for Taterzen's behaviour.
 * <p>
 * Booleans instead of voids are there to allow you to cancel
 * the base Taterzen method.
 * </p>
 */
public interface TaterzenProfession {

    /**
     * Called on Taterzen entity tick.
     * Returning different action results has different meanings:
     * <ul>
     *     <li>{@link InteractionResult#PASS} - Default; continues ticking other professions.</li>
     *     <li>{@link InteractionResult#CONSUME} - Stops processing others, but continues with base Taterzen tick.</li>
     *     <li>{@link InteractionResult#FAIL} - Stops whole movement tick.</li>
     *     <li>{@link InteractionResult#SUCCESS} - Continues with super.tickMovement(), but skips Taterzen's tick.</li>
     * </ul>
     *
     * @return true if you want to cancel the default Taterzen ticking.
     *
     * @deprecated - you can still use {@link TaterzenProfession#tickMovement()} or new event {@link TaterzenProfession#onPlayersNearby(List)}
     * for interactions with players.
     */
    @Deprecated
    default InteractionResult tick() {
        return InteractionResult.PASS;
    }

    /**
     * Called on movement tick.
     * Returning different action results has different meanings:
     * <ul>
     *     <li>{@link InteractionResult#PASS} - Default; continues ticking other professions.</li>
     *     <li>{@link InteractionResult#CONSUME} - Stops processing others, but continues with base Taterzen movement tick.</li>
     *     <li>{@link InteractionResult#FAIL} - Stops whole movement tick.</li>
     *     <li>{@link InteractionResult#SUCCESS} - Continues with super.tickMovement(), but skips Taterzen's movement tick.</li>
     * </ul>
     *
     * @return action result which determines further execution
     */
    default InteractionResult tickMovement() {
        this.tick();
        return InteractionResult.PASS;
    }

    /**
     * Called on Taterzen interaction.
     * @param player player that interacted with Taterzen
     * @param pos pos of interaction
     * @param hand player's hand
     * @return PASS to continue with default interaction, SUCCESS or FAIL to stop.
     */
    default InteractionResult interactAt(Player player, Vec3 pos, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /**
     * Called when Taterzen is attacked.
     *
     * @param attacker entity that is attacking taterzen.
     * @return true to cancel the attack, otherwise false.
     * @deprecated use {@link TaterzenProfession#skipAttackFrom(Entity)} instead. (Same method, but with different name)
     */
    @Deprecated
    default boolean handleAttack(Entity attacker) {
        return this.skipAttackFrom(attacker);
    }


    /**
     * Called when Taterzen is attacked.
     *
     * @param attacker entity that is attacking taterzen.
     * @return true to cancel the attack, otherwise false.
     */
    default boolean skipAttackFrom(Entity attacker) {
        return false;
    }

    /**
     * Called on Taterzen death / removal.
     */
    default void onRemove() {
    }

    /**
     * Called on parsing Taterzen data from {@link CompoundTag}.
     * @param tag tag to load profession data from.
     */
    default void readNbt(CompoundTag tag) {
    }

    /**
     * Called on saving Taterzen data to {@link CompoundTag}.
     * @param tag tag to save profession data to.
     */
    default void saveNbt(CompoundTag tag) {
    }

    /**
     * Method used for creating the new profession for given taterzen.
     *
     * @param taterzen taterzen to create profession for
     * @return new profession object of taterzen.
     * @deprecated Use normal constructor instead.
     */
    @Deprecated
    default TaterzenProfession create(TaterzenNPC taterzen) {
        return new AbstractProfession(taterzen) {};
    }

    /**
     * Called when Taterzen has a chance to pickup an item.
     * You can create a local inventory in the profession and save it there.
     *
     * @param item item entity to be picked up
     * @return true if item should be picked up, otherwise false.

     */
    default boolean tryPickupItem(ItemEntity item) {
        return false;
    }


    /**
     * Called when Taterzen's movement changes.
     * @param movement new movement type.
     */
    default void onMovementSet(NPCData.Movement movement) {
    }

    /**
     * Called when Taterzen's behaviour changes.
     * @param behaviourLevel new behaviour level.
     */
    default void onBehaviourSet(NPCData.Behaviour behaviourLevel) {
    }

    /**
     * Whether to cancel ranged attack.
     * @param target targeted entity.
     * @return false if attack should continue, true to cancel.
     */
    default boolean cancelRangedAttack(LivingEntity target) {
        return false;
    }

    /**
     * Whether to cancel melee attack.
     * @param target targeted entity.
     * @return false if attack should continue, true to cancel.
     */
    default boolean cancelMeleeAttack(Entity target) {
        return false;
    }

    /**
     * Called every tick if players are nearby.
     * @param players players that are in talking range of taterzen.
     */
    default void onPlayersNearby(List<ServerPlayer> players) {
    }

    /**
     * Called when taterzen "loses" this profession.
     * (when it is removed via command / code call)
     */
    default void onProfessionRemoved() {
    }
}
