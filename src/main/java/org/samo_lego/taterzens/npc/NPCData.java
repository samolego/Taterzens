package org.samo_lego.taterzens.npc;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Deafult NPC data.
 */
public class NPCData {
    public EntityType<?> entityType = EntityType.PLAYER;
    public boolean fakeTypeAlive = true;
    public boolean freeWill = false;
    public boolean leashable = true;
    public String command = "";
    public PlayerEntity equipmentEditor = null;
    public long lastActionTime;
    public Movement movement = Movement.NONE;

    public enum Movement {
        NONE,
        LOOK,
        PATH,
        FREE
    }
}
