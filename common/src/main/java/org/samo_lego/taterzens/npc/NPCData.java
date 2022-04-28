package org.samo_lego.taterzens.npc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.compatibility.BungeeCompatibility;

import java.util.ArrayList;
import java.util.UUID;

import static org.samo_lego.taterzens.Taterzens.config;

/**
 * Deafult NPC data.
 * Used for taterzen attributes.
 */
public class NPCData {
    /**
     * Whether Taterzen should be able to be put on a lead..
     */
    public boolean leashable = config.defaults.leashable;
    /**
     * Current equipment editor for Taterzen.
     */
    @Nullable
    public Player equipmentEditor = null;
    /**
     * Default Taterzen movement.
     */
    public Movement movement = Movement.NONE;
    /**
     * Path nodes, used when movement
     * is set to {@link Movement#FORCED_PATH}
     * or {@link Movement#PATH}.
     */
    public ArrayList<BlockPos> pathTargets = new ArrayList<>();
    /**
     * Current index position
     * in {@link NPCData#pathTargets}.
     */
    public int currentMoveTarget = 0;
    /**
     * Whether Taterzen should be pushable.
     */
    public boolean pushable = config.defaults.pushable;
    /**
     * Messages of Taterzen.
     * Saved as &lt;Message Text, Delay&gt;
     */
    public final ArrayList<Pair<Component, Integer>> messages = new ArrayList<>();
    /**
     * Permission level of Taterzen.
     */
    public int permissionLevel = config.defaults.commandPermissionLevel;
    /**
     * Commands to be executed on right click.
     */
    public final ArrayList<String> commands = new ArrayList<>();
    /**
     * Default behaviour of Taterzen.
     */
    public Behaviour behaviour = Behaviour.PASSIVE;
    /**
     * Whether to allow dropping equipment on death.
     */
    public boolean allowEquipmentDrops = false;
    public boolean jumpWhileAttacking = config.defaults.jumpWhileAttacking;

    public final Follow follow = new Follow();
    public boolean allowSounds = !config.defaults.ambientSounds.isEmpty();
    public final ArrayList<Triple<BungeeCompatibility, String, String>> bungeeCommands = new ArrayList<>();
    public boolean allowFlight = config.defaults.allowFlight;
    public boolean allowRiding = config.defaults.allowRiding;
    public long minCommandInteractionTime = config.defaults.minInteractionTime;
    public String commandCooldownMessage = config.defaults.commandCooldownMessage;
    public boolean allowSwimming = config.defaults.allowSwim;

    public static class Follow {
        /**
         * UUID of entity to follow.
         */
        @Nullable
        public UUID targetUuid;
        public FollowTypes type = FollowTypes.NONE;
    }

    /**
     * Types of movement a Taterzen can perform.
     * FORCED types will always follow the type strictly.
     *
     */
    public enum Movement {
        /**
         * No movement at all.
         */
        NONE,
        /**
         * Rotation of the body and head, free at Taterzen's will.
         */
        LOOK,
        /**
         * Rotation of the body and head when a player is in radius of 4 blocks.
         */
        FORCED_LOOK,
        /**
         * Movement to the selected nodes from pathTargets, at Taterzen's will.
         */
        PATH,
        /**
         * Forced movement to the selected nodes from pathTargets.
         */
        FORCED_PATH,
        /**
         * Ticks movement, but does nothing by itself
         */
        TICK,
        /**
         * Wandering around at Taterzen's will.
         */
        FREE
    }

    /**
     * Behaviour types.
     */
    public enum Behaviour {
        /**
         * Doesn't attack. What's a weapon?
         */
        PASSIVE,
        /**
         * Peaceful, but can bite back if attacked.
         */
        DEFENSIVE,
        /**
         * Will turn against hostile mobs.
         */
        FRIENDLY,
        /**
         * Attacks all living creatures.
         */
        HOSTILE
    }

    public enum FollowTypes {
        NONE,
        UUID,
        PLAYERS,
        MOBS
    }
}
