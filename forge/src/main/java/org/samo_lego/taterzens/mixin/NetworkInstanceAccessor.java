package org.samo_lego.taterzens.mixin;

import net.minecraftforge.fml.network.NetworkInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(NetworkInstance.class)
public interface NetworkInstanceAccessor {
    @Accessor(value = "networkProtocolVersion", remap = false)
    String networkProtocolVersion();
    @Accessor(value = "clientAcceptedVersions", remap = false)
    Predicate<String> clientAcceptedVersions();
    @Accessor(value = "serverAcceptedVersions", remap = false)
    Predicate<String> getServerAcceptedVersions();
}
