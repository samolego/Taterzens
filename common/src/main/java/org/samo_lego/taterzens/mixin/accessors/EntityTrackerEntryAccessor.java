package org.samo_lego.taterzens.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerPlayerConnection;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public interface EntityTrackerEntryAccessor {
    @Accessor("serverEntity")
    ServerEntity getPlayer();
    @Accessor("seenBy")
    Set<ServerPlayerConnection> getSeenBy();
}
