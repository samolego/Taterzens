package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundAddMobPacket.class)
public interface MobSpawnS2CPacketAccessor {

    @Accessor("id")
    int getId();

}
