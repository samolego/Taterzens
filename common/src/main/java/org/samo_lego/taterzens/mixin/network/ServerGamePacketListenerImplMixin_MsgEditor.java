package org.samo_lego.taterzens.mixin.network;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.successText;
import static org.samo_lego.taterzens.util.TextUtil.translate;

@Mixin(PlayerList.class)
public class ServerGamePacketListenerImplMixin_MsgEditor {

    /**
     * Catches messages; if player is in
     * message edit mode, messages sent to chat
     * will be saved to taterzen instead.
     */
    @Inject(
            method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatSender;Lnet/minecraft/network/chat/ChatType$Bound;)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void taterzens_chatBroadcast(PlayerChatMessage playerChatMessage, Predicate<ServerPlayer> predicate, ServerPlayer player, ChatSender chatSender, ChatType.Bound bound, CallbackInfo ci) {
        if (player == null) return;

        ITaterzenEditor editor = (ITaterzenEditor) player;
        TaterzenNPC taterzen = editor.getNpc();
        String msg = playerChatMessage.serverContent().getString();

        if (taterzen != null && ((ITaterzenEditor) player).getEditorMode() == ITaterzenEditor.EditorMode.MESSAGES && msg != null && !msg.startsWith("/")) {
            if (msg.startsWith("delay")) {
                String[] split = msg.split(" ");
                if (split.length > 1) {
                    try {
                        int delay = Integer.parseInt(split[1]);
                        taterzen.setMessageDelay(editor.getEditingMessageIndex(), delay);
                        player.displayClientMessage(successText("taterzens.command.message.delay", String.valueOf(delay)), false);
                    } catch (NumberFormatException ignored) {

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
                    text = Component.literal(msg);
                if((editor).getEditingMessageIndex() != -1) {
                    // Editing selected message
                    taterzen.editMessage(editor.getEditingMessageIndex(), text); // Editing message
                    player.displayClientMessage(successText("taterzens.command.message.changed", text.getString()), false);

                    // Exiting the editor
                    if(config.messages.exitEditorAfterMsgEdit) {
                        ((ITaterzenEditor) player).setEditorMode(ITaterzenEditor.EditorMode.NONE);
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