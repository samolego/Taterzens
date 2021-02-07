package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobSpawnS2CPacket.class)
public interface MobSpawnS2CPacketAccessor {

    @Accessor("id")
    int getId();

    @Accessor("entityTypeId")
    void setEntityTypeId(int entityTypeId);
}
