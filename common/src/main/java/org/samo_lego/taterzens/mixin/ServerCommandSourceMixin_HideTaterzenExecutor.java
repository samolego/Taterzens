package org.samo_lego.taterzens.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.taterzens.Taterzens.config;

@Mixin(ServerCommandSource.class)
public class ServerCommandSourceMixin_HideTaterzenExecutor {
    @Shadow @Final @Nullable private Entity entity;

    @Inject(
            method = "sendToOps(Lnet/minecraft/text/Text;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void cancelSendingToOps(Text message, CallbackInfo ci) {
        if(this.entity instanceof TaterzenNPC && config.hideOpsMessage) {
            ci.cancel();
        }
    }
}
