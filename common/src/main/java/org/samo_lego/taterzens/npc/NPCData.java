package org.samo_lego.taterzens.npc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

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
    public PlayerEntity equipmentEditor = null;
    /**
     * Default Taterzen movement.
     */
    public Movement movement = Movement.NONE;
    /**
     * Path nodes, used when movement
     * is set to {@link NPCData.Movement#FORCED_PATH}
     * or {@link NPCData.Movement#PATH}.
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
    public ArrayList<Pair<Text, Integer>> messages = new ArrayList<>();
    /**
     * Permission level of Taterzen.
     */
    public int permissionLevel = config.defaults.commandPermissionLevel;
    /**
     * Commands to be executed on right click.
     */
    public ArrayList<String> commands = new ArrayList<>();
    /**
     * Default behaviour of Taterzen.
     */
    public Behaviour behaviour = Behaviour.PASSIVE;
    /**
     * Whether to allow dropping equipment on death.
     */
    public boolean allowEquipmentDrops = false;
    public boolean jumpWhileAttacking = config.defaults.jumpWhileAttacking;

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
}
