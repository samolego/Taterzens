package org.samo_lego.taterzens.npc;

import net.minecraft.entity.EntityType;

/**
 * Deafult NPC data.
 */
public class NPCData {
    public EntityType<?> entityType = EntityType.PLAYER;
    public boolean fakeTypeAlive = true;
    public boolean freeWill = false;
    public boolean stationary = true;
    public boolean leashable = true;
    public String command = "";
}
