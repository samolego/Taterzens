package org.samo_lego.taterzens.mixin;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
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
            method = "createPacket",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false
    )
    private static void removeTaterzenFromSync(CallbackInfoReturnable<Packet<?>> cir, CompoundTag tag, PacketByteBuf buf) {
        CompoundTag registries = tag.getCompound("registries");
        if(registries != null) {
            CompoundTag entityTypes = registries.getCompound("minecraft:entity_type");
            if(entityTypes != null) {
                entityTypes.remove(MODID + ":npc");
                buf.writeCompoundTag(tag);
            }
        }
    }
}
