package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(EntityTrackerUpdateS2CPacket.class)
public interface EntityTrackerUpdateS2CPacketAccessor {
    @Accessor("id")
    int getId();

    @Accessor("trackedValues")
    void setTrackedValues(List<DataTracker.Entry<?>> trackedValues);
}
