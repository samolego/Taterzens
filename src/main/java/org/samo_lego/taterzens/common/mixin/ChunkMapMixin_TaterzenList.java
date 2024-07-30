package org.samo_lego.taterzens.common.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.samo_lego.taterzens.common.Taterzens;
import org.samo_lego.taterzens.common.npc.TaterzenNPC;

import static org.samo_lego.taterzens.common.Taterzens.TATERZEN_NPCS;

@Mixin(ChunkMap.class)
public class ChunkMapMixin_TaterzenList {

    /**
     * Sets Taterzen to {@link Taterzens#TATERZEN_NPCS NPC list}.
     * @param entity entity being loaded
     */
    @Inject(method = "addEntity(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void onEntityAdded(Entity entity, CallbackInfo ci) {
        if (entity instanceof TaterzenNPC && !TATERZEN_NPCS.containsKey(entity.getUUID())) {
            TATERZEN_NPCS.put(entity.getUUID(), (TaterzenNPC) entity);
        }
    }

    /**
     * Unloads Taterzen from {@link Taterzens#TATERZEN_NPCS NPC list}.
     * @param entity entity being unloaded
     */
    @Inject(method = "removeEntity(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void onEntityRemoved(Entity entity, CallbackInfo ci) {
        if (entity instanceof TaterzenNPC) {
            TATERZEN_NPCS.remove(entity.getUUID());
        }
    }
}
