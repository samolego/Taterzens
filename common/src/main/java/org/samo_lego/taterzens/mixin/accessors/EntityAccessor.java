package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("dataTracker")
    void setDataTracker(DataTracker dataTracker);
}
