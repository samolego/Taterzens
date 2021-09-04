package org.samo_lego.taterzens.mixin;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.successText;
import static org.samo_lego.taterzens.util.TextUtil.translate;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin_MsgEditor {
    @Shadow public ServerPlayer player;

    /**
     * Catches messages; if player is in
     * message edit mode, messages sent to chat
     * will be saved to taterzen instead.
     *
     * @param message message sent by player
     */
    @Inject(
            method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void onMessage(TextFilter.FilteredText message, CallbackInfo ci) {
        ITaterzenEditor editor = (ITaterzenEditor) this.player;
        TaterzenNPC taterzen = editor.getNpc();
        String msg = message.getFiltered();
        if(taterzen != null && ((ITaterzenEditor) this.player).getEditorMode() == ITaterzenEditor.EditorMode.MESSAGES && !msg.startsWith("/")) {
            if(msg.startsWith("delay")) {
                String[] split = msg.split(" ");
                if(split.length > 1) {
                    try {
                        int delay = Integer.parseInt(split[1]);
                        taterzen.setMessageDelay(editor.getEditingMessageIndex(), delay);
                        this.player.displayClientMessage(successText("taterzens.command.message.delay", String.valueOf(delay)), false);
                    } catch(NumberFormatException ignored) {

                    }
                }
            } else {
                Component text;
                if((msg.startsWith("{") && msg.endsWith("}") || (msg.startsWith("[") && msg.endsWith("]")))) {
                    // NBT tellraw message structure, try parse it
                    try {
                        text = Component.Serializer.fromJson(new StringReader(msg));
                    } catch(JsonParseException ignored) {
                        player.displayClientMessage(translate("taterzens.error.invalid.text").withStyle(ChatFormatting.RED), false);
                        ci.cancel();
                        return;
                    }
                } else
                    text = new TextComponent(msg);
                if((editor).getEditingMessageIndex() != -1) {
                    // Editing selected message
                    taterzen.editMessage(editor.getEditingMessageIndex(), text); // Editing message
                    player.displayClientMessage(successText("taterzens.command.message.changed", text.getString()), false);

                    // Exiting the editor
                    if(config.messages.exitEditorAfterMsgEdit) {
                        ((ITaterzenEditor) this.player).setEditorMode(ITaterzenEditor.EditorMode.NONE);
                        (editor).setEditingMessageIndex(-1);
                        player.displayClientMessage(translate("taterzens.command.equipment.exit").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                    }
                } else {
                    taterzen.addMessage(text); // Adding message
                    player.displayClientMessage(successText("taterzens.command.message.editor.add", text.getString()), false);
                }

            }
            ci.cancel();
        }
    }
}
