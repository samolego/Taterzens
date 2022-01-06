package org.samo_lego.taterzens.fabric.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

import static org.samo_lego.taterzens.Taterzens.MOD_ID;


@Mixin(RegistrySyncManager.class)
public class RegistrySyncManagerMixin_TaterzenSyncDisabler {

    private static final ResourceLocation ENTITY_TYPE = new ResourceLocation("minecraft", "entity_type");

    /**
     * Removes taterzen tag from registry sync, as we do not need it on client.
     * Prevents client from being kicked if using FAPI.
     */
    @Inject(
            method = "createAndPopulateRegistryMap",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false
    )
    private static void removeTaterzenFromSync(boolean isClientSync,
                                               @Nullable Map<ResourceLocation,
                                               Object2IntMap<ResourceLocation>> activeMap,
                                               CallbackInfoReturnable<Map<ResourceLocation,
                                               Object2IntMap<ResourceLocation>>> cir,
                                               Map<ResourceLocation, Object2IntMap<ResourceLocation>> map
    ) {
        if (isClientSync) {
            map.get(ENTITY_TYPE).removeInt(new ResourceLocation(MOD_ID, "npc"));
        }
    }
}
