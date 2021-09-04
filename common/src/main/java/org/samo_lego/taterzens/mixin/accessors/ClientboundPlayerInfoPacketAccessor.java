package org.samo_lego.taterzens.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;

@Mixin(ClientboundPlayerInfoPacket.class)
public interface ClientboundPlayerInfoPacketAccessor {
    @Accessor("action")
    void setAction(ClientboundPlayerInfoPacket.Action action);

    @Accessor("action")
    ClientboundPlayerInfoPacket.Action getAction();

    @Mutable
    @Accessor("entries")
    void setEntries(List<ClientboundPlayerInfoPacket.PlayerUpdate> entries);
    @Accessor("entries")
    List<ClientboundPlayerInfoPacket.PlayerUpdate> getEntries();
}
