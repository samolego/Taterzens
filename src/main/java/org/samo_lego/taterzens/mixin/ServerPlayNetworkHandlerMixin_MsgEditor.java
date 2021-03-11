package org.samo_lego.taterzens.mixin;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.samo_lego.taterzens.util.TextUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.Taterzens.lang;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_MsgEditor {
    @Shadow public ServerPlayerEntity player;

    /**
     * Catches messages; if player is in
     * message edit mode, messages sent to chat
     * will be saved to taterzen instead.
     *
     * @param msg
     * @param ci
     */
    @Inject(
            method = "method_31286(Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void onMessage(String msg, CallbackInfo ci) {
        TaterzenNPC taterzen = ((TaterzenEditor) this.player).getNpc();
        if(taterzen != null && ((TaterzenEditor) this.player).inMsgEditMode() && !msg.startsWith("/")) {
            if(msg.startsWith("delay")) {
                String[] split = msg.split(" ");
                if(split.length > 1) {
                    try {
                        int delay = Integer.parseInt(split[1]);
                        //taterzen.setMessageDelay(delay);
                        player.sendMessage(TextUtil.successText(lang.success.messageDelaySet, new LiteralText(String.valueOf(delay))), false);
                    } catch(NumberFormatException ignored) {

                    }
                }
            } else {
                Text text;
                if(msg.startsWith("{") && msg.endsWith("}")) {
                    // NBT tellraw message structure, try parse it
                    try {
                        text = Text.Serializer.fromJson(new StringReader(msg));
                    } catch(JsonParseException ignored) {
                        player.sendMessage(new LiteralText(lang.error.invalidText).formatted(Formatting.RED), false);
                        ci.cancel();
                        return;
                    }
                } else
                    text = new LiteralText(msg);
                if(((TaterzenEditor) player).getMessageEditing() != -1) {
                    // Editing selected message
                    taterzen.setMessage(((TaterzenEditor) player).getMessageEditing(), text); // Editing message
                    player.sendMessage(TextUtil.successText(lang.success.messageChanged, text), false);

                    // Exiting the editor
                    if(config.messages.exitEditorAfterMsgEdit) {
                        ((TaterzenEditor) player).setMsgEditMode(false);
                        ((TaterzenEditor) player).setMessageEditing(-1);
                        player.sendMessage(new LiteralText(lang.success.editorExit).formatted(Formatting.LIGHT_PURPLE), false);
                    }
                } else {
                    taterzen.addMessage(text); // Adding message
                    player.sendMessage(TextUtil.successText(lang.success.messageAdded, text), false);
                }

            }
            ci.cancel();
        }
    }
}
