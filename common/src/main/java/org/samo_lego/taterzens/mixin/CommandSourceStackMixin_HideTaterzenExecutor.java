package org.samo_lego.taterzens.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.taterzens.Taterzens.config;

@Mixin(CommandSourceStack.class)
public class CommandSourceStackMixin_HideTaterzenExecutor {
    @Shadow @Final @Nullable private Entity entity;

    @Inject(
            method = "broadcastToAdmins(Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void cancelSendingToOps(Component message, CallbackInfo ci) {
        if(this.entity instanceof TaterzenNPC && config.hideOpsMessage) {
            ci.cancel();
        }
    }
}
