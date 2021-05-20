package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerCommandSource.class)
public interface ServerCommandSourceAccessor {
    @Accessor("level")
    int getPermissionLevel();
}
