package org.samo_lego.taterzens.mixin;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static org.samo_lego.taterzens.Taterzens.MODID;
import static org.samo_lego.taterzens.Taterzens.config;


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
            remap = false
    )
    private static void removeTaterzenFromSync(boolean isClientSync, CompoundTag activeTag, CallbackInfoReturnable<CompoundTag> cir, CompoundTag mainTag, CompoundTag tag) {
        if(config.disableRegistrySync) {
            CompoundTag registries = tag.getCompound("registries");
            CompoundTag entityTypes = registries.getCompound("minecraft:entity_type");
            if(entityTypes != null) {
                entityTypes.remove(MODID + ":npc");
            }
        }
    }
}
