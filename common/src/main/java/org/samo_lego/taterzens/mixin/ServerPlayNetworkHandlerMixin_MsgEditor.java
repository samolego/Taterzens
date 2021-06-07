package org.samo_lego.taterzens.mixin;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.successText;
import static org.samo_lego.taterzens.util.TextUtil.translate;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_MsgEditor {
    @Shadow public ServerPlayerEntity player;

    /**
     * Catches messages; if player is in
     * message edit mode, messages sent to chat
     * will be saved to taterzen instead.
     *
     * @param message message sent by player
     */
    @Inject(
            method = "handleMessage(Lnet/minecraft/server/filter/TextStream$Message;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void onMessage(TextStream.Message message, CallbackInfo ci) {
        TaterzenEditor editor = (TaterzenEditor) this.player;
        TaterzenNPC taterzen = editor.getNpc();
        String msg = message.getFiltered();
        if(taterzen != null && ((TaterzenEditor) this.player).getEditorMode() == TaterzenEditor.Types.MESSAGES && !msg.startsWith("/")) {
            if(msg.startsWith("delay")) {
                String[] split = msg.split(" ");
                if(split.length > 1) {
                    try {
                        int delay = Integer.parseInt(split[1]);
                        taterzen.setMessageDelay(editor.getEditingMessageIndex(), delay);
                        this.player.sendMessage(successText("taterzens.command.message.delay", String.valueOf(delay)), false);
                    } catch(NumberFormatException ignored) {

                    }
                }
            } else {
                Text text;
                if((msg.startsWith("{") && msg.endsWith("}") || (msg.startsWith("[") && msg.endsWith("]")))) {
                    // NBT tellraw message structure, try parse it
                    try {
                        text = Text.Serializer.fromJson(new StringReader(msg));
                    } catch(JsonParseException ignored) {
                        player.sendMessage(translate("taterzens.error.invalid.text").formatted(Formatting.RED), false);
                        ci.cancel();
                        return;
                    }
                } else
                    text = new LiteralText(msg);
                if((editor).getEditingMessageIndex() != -1) {
                    // Editing selected message
                    taterzen.editMessage(editor.getEditingMessageIndex(), text); // Editing message
                    player.sendMessage(successText("taterzens.command.message.changed", text.getString()), false);

                    // Exiting the editor
                    if(config.messages.exitEditorAfterMsgEdit) {
                        ((TaterzenEditor) this.player).setEditorMode(TaterzenEditor.Types.NONE);
                        (editor).setEditingMessageIndex(-1);
                        player.sendMessage(translate("taterzens.command.equipment.exit").formatted(Formatting.LIGHT_PURPLE), false);
                    }
                } else {
                    taterzen.addMessage(text); // Adding message
                    player.sendMessage(successText("taterzens.command.message.editor.add", text.getString()), false);
                }

            }
            ci.cancel();
        }
    }
}
