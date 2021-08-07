package org.samo_lego.taterzens.mixin;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static org.samo_lego.taterzens.Taterzens.MODID;


@Mixin(RegistrySyncManager.class)
public class RegistrySyncManagerMixin_TaterzenSyncDisabler {

    /**
     * Removes taterzen tag from registry sync, as we do not need it on client.
     * Prevents client from being kicked if using FAPI.
     */
    @Inject(
            method = "toTag",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false,
            cancellable = true
    )
    private static void removeTaterzenFromSync(boolean isClientSync, NbtCompound activeTag, CallbackInfoReturnable<NbtCompound> cir, NbtCompound mainTag, NbtCompound tag) {
        NbtCompound registries = tag.getCompound("registries");
        NbtCompound entityTypes = registries.getCompound("minecraft:entity_type");
        if(entityTypes != null) {
            entityTypes.remove(MODID + ":npc");
            for (String key : entityTypes.getKeys()) {
                // May cause problems if a mod registers entity types using minecraft namespace
                if (!key.startsWith("minecraft:")) {
                    cir.setReturnValue(tag);
                    return;
                }
            }
            registries.remove("minecraft:entity_type");
        }
    }
}
