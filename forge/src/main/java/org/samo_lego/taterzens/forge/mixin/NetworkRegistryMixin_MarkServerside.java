package org.samo_lego.taterzens.forge.mixin;

import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;

@Mixin(value = NetworkRegistry.class, remap = false)
public class NetworkRegistryMixin_MarkServerside {

    /**
     * Extremely cursed. I know. I shouldn't be doing this.
     * But Forge doesn't allow server-only-registries.
     */
    /*@Inject(
            method = "listRejectedVanillaMods(Ljava/util/function/BiFunction;)Ljava/util/List;",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Ljava/util/Map;values()Ljava/util/Lis;"
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
    }*/
}
