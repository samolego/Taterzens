package org.samo_lego.taterzens.event;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.samo_lego.taterzens.compatibility.BungeeCompatibility;

import static org.samo_lego.taterzens.compatibility.BungeeCompatibility.AVAILABLE_SERVERS;
import static org.samo_lego.taterzens.Taterzens.config;

public class PlayerJoinEvent {

    /**todo
     * Used if player is in path edit mode. Interacted blocks are removed from the path
     * of selected {@link org.samo_lego.taterzens.npc.TaterzenNPC}.
     *
     * @param handler player's packet listener
     */
    public static void onPlayerJoin(ServerGamePacketListenerImpl handler) {
        if(AVAILABLE_SERVERS.isEmpty() && config.bungee.enableCommands) {
            System.out.println("Player join: " + handler.getPlayer().getName().getString());
            // Fetch available servers from proxy
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("GetServers");
            BungeeCompatibility.sendProxyPacket(handler, out.toByteArray());
        }
    }
}
