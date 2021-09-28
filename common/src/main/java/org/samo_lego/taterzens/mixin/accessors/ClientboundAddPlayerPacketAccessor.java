package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(ClientboundAddPlayerPacket.class)
public interface ClientboundAddPlayerPacketAccessor {
    @Mutable
    @Accessor("entityId")
    void setId(int id);
    @Mutable
    @Accessor("entityId")
    int getId();

    @Mutable
    @Accessor("playerId")
    void setUuid(UUID uuid);

    @Mutable
    @Accessor("x")
    void setX(double x);

    @Mutable
    @Accessor("y")
    void setY(double y);

    @Mutable
    @Accessor("z")
    void setZ(double z);

    @Mutable
    @Accessor("yRot")
    void setYRot(byte yRot);

    @Mutable
    @Accessor("xRot")
    void setXRot(byte xRot);
}
