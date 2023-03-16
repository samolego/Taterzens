package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSourceStack.class)
public interface ACommandSourceStack {
    @Accessor("permissionLevel")
    int getPermissionLevel();
}
