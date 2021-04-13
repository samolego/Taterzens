package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(PlayerSpawnS2CPacket.class)
public interface PlayerSpawnS2CPacketAccessor {
    @Accessor("id")
    void setId(int id);
    @Accessor("id")
    int getId();

    @Accessor("uuid")
    void setUuid(UUID uuid);

    @Accessor("x")
    void setX(double x);

    @Accessor("y")
    void setY(double y);

    @Accessor("z")
    void setZ(double z);

    @Accessor("yaw")
    void setYaw(byte yaw);

    @Accessor("pitch")
    void setPitch(byte pitch);
}
