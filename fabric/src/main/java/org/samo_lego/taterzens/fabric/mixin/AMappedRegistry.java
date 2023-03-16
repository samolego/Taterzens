package org.samo_lego.taterzens.fabric.mixin;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MappedRegistry.class)
public interface AMappedRegistry<T> {
    @Accessor("byId")
    ObjectList<Holder.Reference<T>> getById();
}
