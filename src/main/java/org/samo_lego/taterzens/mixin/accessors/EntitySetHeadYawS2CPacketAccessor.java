package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySetHeadYawS2CPacket.class)
public interface EntitySetHeadYawS2CPacketAccessor {
    @Accessor("entity")
    int getEntityId();
}
