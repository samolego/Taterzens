package org.samo_lego.taterzens.mixin;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static org.samo_lego.taterzens.Taterzens.MODID;

@Mixin(RegistrySyncManager.class)
public class RegistrySyncManagerMixin_TaterzenSyncDisabler {

    /**
     * Removes taterzen tag from registry sync, as we do not need it on client.
     * Prevents client from being kicked if using FAPI.
     */
    @ModifyVariable(
            method = "createPacket()Lnet/minecraft/network/Packet;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/PacketByteBuf;writeCompoundTag(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/network/PacketByteBuf;"
            ),
            remap = false
    )
    private static CompoundTag removeTaterzenFromSync(CompoundTag tag) {
        if(tag != null) {
            CompoundTag registries = tag.getCompound("registries");
            if(registries != null) {
                CompoundTag entityTypes = registries.getCompound("minecraft:entity_type");
                if(entityTypes != null) {
                    entityTypes.remove(MODID + ":npc");
                }
            }
        }
        return tag;
    }
}
