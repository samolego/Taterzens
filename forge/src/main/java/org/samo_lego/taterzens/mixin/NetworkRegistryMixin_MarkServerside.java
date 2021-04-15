package org.samo_lego.taterzens.mixin;

import net.minecraft.util.Identifier;
import net.minecraftforge.fml.network.NetworkInstance;
import net.minecraftforge.fml.network.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Mixin(NetworkRegistry.class)
public class NetworkRegistryMixin_MarkServerside {

    @Shadow private static Map<Identifier, NetworkInstance> instances;

    /**
     * Extremely cursed. I know. I shouldn't be doing this.
     * But Forge doesn't allow server-only-registries.
     */
    @Inject(
            method = "listRejectedVanillaMods(Ljava/util/function/BiFunction;)Ljava/util/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;isEmpty()Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false,
            cancellable = true
    )
    private static void taterzens$removeTaterzensFromRegistrySync(BiFunction<NetworkInstance, String, Boolean> testFunction, CallbackInfoReturnable<List<String>> cir, List<String> results) {
        results.remove("taterzens");
        System.out.println(results);
        instances.values().forEach(networkInstance -> {
             System.out.println("Checking: split");
             System.out.println(((NetworkInstanceAccessor) networkInstance).networkProtocolVersion());
             System.out.println(((NetworkInstanceAccessor) networkInstance).clientAcceptedVersions());
             System.out.println(((NetworkInstanceAccessor) networkInstance).getServerAcceptedVersions());
        });
        cir.setReturnValue(Collections.emptyList());
    }
}
