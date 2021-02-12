package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySpawnS2CPacket.class)
public interface EntitySpawnS2CPacketAccessor {
    /**
     * Sets the entity type. Used for faking how the client sees {@link org.samo_lego.taterzens.npc.TaterzenNPC}.
     * @param entityType faske entity type of the TaterzenNPC
     */
    @Accessor("entityTypeId")
    void setEntityId(EntityType<?> entityType);
}
