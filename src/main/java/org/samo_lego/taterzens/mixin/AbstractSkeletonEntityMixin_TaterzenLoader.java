package org.samo_lego.taterzens.mixin;

import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.nbt.CompoundTag;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Takes care of loading {@link org.samo_lego.taterzens.npc.TaterzenNPC} from tag.
 */
@Mixin(AbstractSkeletonEntity.class)
public class AbstractSkeletonEntityMixin_TaterzenLoader {

    @Inject(
            method = "readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At(
                    value = "TAIL"
            )
    )
    private void loadTaterzenFromTag(CompoundTag tag, CallbackInfo ci) {
        if(tag.contains("TaterzenNPCTag")) {
            System.out.println("Loading taterzen!");
            ((TaterzenNPC) (Object) this).fromTag(tag);
        }
    }
}
