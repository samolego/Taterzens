package org.samo_lego.taterzens.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(EntityType.class)
public class EntityTypeMixin {

    /**
     * Checks if entity is a {@link org.samo_lego.taterzens.npc.TaterzenNPC}.
     * If yes, entity id is changed to skeleton so TaterzenNPC can be loaded.
     *
     * If we'd save it as skeleton from the {@link org.samo_lego.taterzens.npc.TaterzenNPC#toTag(CompoundTag) begining},
     * we'd have skeletons running around after uninstalling the mod :P
     *
     * @param tag
     * @param world
     * @param cir
     */
    @Inject(method = "getEntityFromTag(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/World;)Ljava/util/Optional;", at = @At("HEAD"))
    private static void checkEntityTag(CompoundTag tag, World world, CallbackInfoReturnable<Optional<Entity>> cir) {
        if(tag.getString("id").equals("taterzens:npc")) {
            tag.putString("id", "minecraft:skeleton");
        }
    }
}
