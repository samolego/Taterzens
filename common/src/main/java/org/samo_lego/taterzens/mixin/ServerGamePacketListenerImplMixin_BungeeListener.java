package org.samo_lego.taterzens.mixin;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.samo_lego.taterzens.compatibility.BungeeCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

import static net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket.BRAND;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.BungeeCompatibility.AVAILABLE_SERVERS;

/**
 * Handles bungee packets.
 */
@Mixin(value = ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin_BungeeListener {
    @Inject(method = "handleCustomPayload", at = @At("TAIL"))
    private void onCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        ResourceLocation packetId = packet.getIdentifier();

        if(AVAILABLE_SERVERS.isEmpty() && config.bungee.enableCommands) {
            if(packetId.equals(BungeeCompatibility.BUNGEE_CHANNEL)) {
                // Reading data
                byte[] bytes = new byte[packet.getData().readableBytes()];
                packet.getData().readBytes(bytes);

                // Parsing the response
                if(bytes.length != 0) {
                    ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
                    String subchannel = in.readUTF();

                    if(subchannel.equals("GetServers")) {
                        // Adding available servers to suggestions
                        String[] servers = in.readUTF().split(", ");
                        Collections.addAll(AVAILABLE_SERVERS, servers);
                    }
                }
            } else if(packetId.equals(BRAND)) {
                // Fetch available servers from proxy
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("GetServers");
                BungeeCompatibility.sendProxyPacket((ServerGamePacketListenerImpl) (Object) this, out.toByteArray());
            }
        }
    }
}
