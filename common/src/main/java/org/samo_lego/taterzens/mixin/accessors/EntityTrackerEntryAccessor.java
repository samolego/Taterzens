package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(targets = "net.minecraft.server.world.ThreadedAnvilChunkStorage$EntityTracker")
public interface EntityTrackerEntryAccessor {
    @Accessor("entry")
    EntityTrackerEntry getEntry();
    @Accessor("playersTracking")
    Set<ServerPlayerEntity> getTrackingPlayers();
}
