package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("FLAGS")
    static TrackedData<Byte> getFLAGS() {
        throw new AssertionError();
    }

    @Accessor("GLOWING_FLAG_INDEX")
    static int getGLOWING_FLAG_INDEX() {
        throw new AssertionError();
    }
}
