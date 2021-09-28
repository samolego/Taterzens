package org.samo_lego.taterzens.forge.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkInstance;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static org.samo_lego.taterzens.Taterzens.config;

@Mixin(value = NetworkRegistry.class, remap = false)
public class NetworkRegistryMixin_MarkServerside {

    /**
     * Extremely cursed. I know. I shouldn't be doing this.
     * But Forge doesn't allow server-only-registries.
     */
    @Inject(
            method = "listRejectedVanillaMods(Ljava/util/function/BiFunction;)Ljava/util/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;values()Ljava/util/Collection;",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private static void taterzens$removeTaterzensFromRegistrySync(BiFunction<NetworkInstance, String, Boolean> testFunction, CallbackInfoReturnable<List<String>> cir, List<Pair<ResourceLocation, Boolean>> results) {
        System.out.println(results);
        //results.remove("taterzens");
        if(config.disableRegistrySync) {
            /*System.out.println(results);
            instances.values().forEach(networkInstance -> {
                System.out.println("Checking: split");
                System.out.println(((NetworkInstanceAccessor) networkInstance).networkProtocolVersion());
                System.out.println(((NetworkInstanceAccessor) networkInstance).clientAcceptedVersions());
                System.out.println(((NetworkInstanceAccessor) networkInstance).getServerAcceptedVersions());
            });*/
            cir.setReturnValue(Collections.emptyList());
        }
    }
}
