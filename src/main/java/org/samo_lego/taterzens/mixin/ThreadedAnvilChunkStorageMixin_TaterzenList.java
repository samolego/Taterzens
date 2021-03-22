package org.samo_lego.taterzens.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.taterzens.Taterzens.TATERZEN_NPCS;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin_TaterzenList {

    /**
     * Sets Taterzen to {@link org.samo_lego.taterzens.Taterzens#TATERZEN_NPCS NPC list}.
     * @param entity entity being loaded
     * @param ci
     */
    @Inject(method = "loadEntity(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
    private void onEntityLoad(Entity entity, CallbackInfo ci) {
        if(entity instanceof TaterzenNPC)
            TATERZEN_NPCS.add((TaterzenNPC) entity);
    }

    /**
     * Unloads Taterzen from {@link org.samo_lego.taterzens.Taterzens#TATERZEN_NPCS NPC list}.
     * @param entity entity being unloaded
     * @param ci
     */
    @Inject(method = "unloadEntity(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
    private void onEntityUnload(Entity entity, CallbackInfo ci) {
        if(entity instanceof TaterzenNPC)
            TATERZEN_NPCS.remove(entity);
    }
}
