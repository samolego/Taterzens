package org.samo_lego.taterzens.compatibility;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.Arrays;
import java.util.Collections;

import static net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket.BRAND;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.BungeeCompatibility.AVAILABLE_SERVERS;

public class Tmp {
    public static void mixinRepace(ServerboundCustomPayloadPacket packet, ServerGamePacketListenerImpl impl) {
        ResourceLocation packetId = packet.getIdentifier();

        if(AVAILABLE_SERVERS.isEmpty() && config.bungee.enableCommands) {
            System.out.println(packet.getData().readableBytes());
            System.out.println(packet.getData().readerIndex());
            if(packetId.equals(BungeeCompatibility.BUNGEE_CHANNEL)) {
                byte[] bytes = packet.getData().readByteArray();
                System.out.println(Arrays.toString(bytes) + " " + bytes.length);
                System.out.println("READING");

                // Parsing the response
                if(bytes.length != 0) {
                    ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
                    String subchannel = in.readUTF();
                    System.out.println(subchannel);

                    if(subchannel.equals("GetServers")) {
                        // Adding available servers to suggestions
                        String[] servers = in.readUTF().split(", ");
                        Collections.addAll(AVAILABLE_SERVERS, servers);
                    }
                }
                else {
                    System.out.println("EMPTY");
                    System.out.println(packet.getData().readableBytes());
                    System.out.println(packet.getData().readerIndex());
                }
            } else if(packetId.equals(BRAND)) {
                // Fetch available servers from proxy
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("GetServers");
                BungeeCompatibility.sendProxyPacket(impl, out.toByteArray());
            }
        }
    }
}
