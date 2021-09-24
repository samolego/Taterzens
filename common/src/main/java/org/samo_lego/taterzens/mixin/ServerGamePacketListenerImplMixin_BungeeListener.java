package org.samo_lego.taterzens.mixin;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.samo_lego.taterzens.compatibility.BungeeCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.BungeeCompatibility.AVAILABLE_SERVERS;

/**
 * Handles received bungee packets.
 * Currently used to listen to response to {@link org.samo_lego.taterzens.event.PlayerJoinEvent#onPlayerJoin(ServerGamePacketListenerImpl)}.
 */
@Mixin(value = ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin_BungeeListener {
    @Inject(method = "handleCustomPayload", at = @At("TAIL"))
    private void onCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        System.out.println(packet.getIdentifier() + " " + packet.getData());
        if(packet.getIdentifier().equals(BungeeCompatibility.BUNGEE_CHANNEL) && AVAILABLE_SERVERS.isEmpty() && config.bungee.enableCommands) {
            // Adding available servers to suggestions
            ByteArrayDataInput in = ByteStreams.newDataInput(packet.getData().readByteArray());
            String subchannel = in.readUTF();
            System.out.println(subchannel);

            if(subchannel.equals("GetServers")) {
                String[] servers = in.readUTF().split(", ");
                Collections.addAll(AVAILABLE_SERVERS, servers);
            }
        }
    }
}
