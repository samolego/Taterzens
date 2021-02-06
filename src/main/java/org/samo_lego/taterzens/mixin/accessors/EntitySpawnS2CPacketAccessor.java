package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySpawnS2CPacket.class)
public interface EntitySpawnS2CPacketAccessor {
    @Accessor("id")
    void setId(int id);
    @Accessor("id")
    int getId();
}
