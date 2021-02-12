package org.samo_lego.taterzens.npc;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

import static org.samo_lego.taterzens.Taterzens.config;

/**
 * Deafult NPC data.
 */
public class NPCData {
    public EntityType<?> entityType = EntityType.PLAYER;
    public boolean fakeTypeAlive = true;
    /**
     * Used for att
     */
    public boolean hostile = false;
    public boolean leashable = config.defaults.leashable;
    public String command = "";
    public PlayerEntity equipmentEditor = null;
    public long lastActionTime;
    public Movement movement = Movement.NONE;
    public ArrayList<BlockPos> pathTargets = new ArrayList<>();
    public int currentMoveTarget = 0;
    public boolean pushable = false;

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
         * Rotation of the body & head, free at Taterzen's will.
         */
        LOOK,
        /**
         * Rotation of the body & head when a player is in radius of 4 blocks.
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
}
