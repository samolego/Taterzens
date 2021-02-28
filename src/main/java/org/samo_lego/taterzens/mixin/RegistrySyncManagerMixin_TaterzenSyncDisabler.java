package org.samo_lego.taterzens.mixin;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistrySyncManager.class)
public class RegistrySyncManagerMixin_TaterzenSyncDisabler {

    /**
     * Removes taterzen tag from registry sync, as we do not need it on client.
     * Prevents client from being kicked if using FAPI.
     */
    /*@Inject(
            method = "createPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/PacketByteBuf;writeCompoundTag(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/network/PacketByteBuf;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false
    )
    private static void removeTaterzenFromSync(CallbackInfoReturnable<Packet<?>> cir, CompoundTag tag, PacketByteBuf _buf) {
        CompoundTag registries = tag.getCompound("registries");
        if(registries != null) {
            CompoundTag entityTypes = registries.getCompound("minecraft:entity_type");
            if(entityTypes != null) {
                entityTypes.remove(MODID + ":npc");
            }
        }
    }*/
}
