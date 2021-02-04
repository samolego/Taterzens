package org.samo_lego.taterzens.npc;

import net.minecraft.entity.EntityType;

/**
 * Deafult NPC data.
 */
public class NPCData {
    public EntityType<?> entityType = EntityType.PLAYER;
    public boolean freeWill = false;
    public boolean gravity = false;
    public boolean stationary = true;
    public boolean invulnerable = true;
    public boolean leashable;
}
