package org.samo_lego.taterzens.compatibility;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.apache.commons.lang3.tuple.MutableTriple;

import java.util.ArrayList;

/**
 * Available / supported bungee commands.
 *
 * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#wikiPage">Spigot thread</a> on message channels.
 * @see <a href="https://github.com/VelocityPowered/Velocity/blob/65db0fad6a221205ec001f1f68a032215da402d6/proxy/src/main/java/com/velocitypowered/proxy/connection/backend/BungeeCordMessageResponder.java#L297">Proxy implementation</a> on GitHub.
 */
public enum BungeeCompatibility {
    SERVER("ConnectOther"),
    MESSAGE("Message"),
    MESSAGE_RAW("MessageRaw"),
    KICK("KickPlayer");

    /**
     * Contains all available proxy servers.
     */
    public static final ArrayList<String> AVAILABLE_SERVERS = new ArrayList<>();

    /**
     * Identifier of the proxy message channel.
     */
    public static final ResourceLocation BUNGEE_CHANNEL = new ResourceLocation("bungeecord", "main");

    private final String subchannel;

    BungeeCompatibility(String subchannel) {
        this.subchannel = subchannel;
    }

    /**
     * Gets the subchannel of enum.
     * @return proxy subchannel.
     */
    public String getSubchannel() {
        return subchannel;
    }

    /**
     * Sends a packet to proxy.
     * @param connection connection to use for sending packet.
     * @param data data to sent in the packet.
     */
    public static void sendProxyPacket(ServerGamePacketListenerImpl connection, byte[] data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBytes(data);

        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(BUNGEE_CHANNEL, buf);
        connection.send(packet);
    }

    /**
     * A triple used for bungee command saving with overriden {@link Object#toString()} method
     * for nicer printing when removing command from npc.
     */
    public static class BungeeCommand extends MutableTriple<BungeeCompatibility, String, String> {
        public BungeeCommand(BungeeCompatibility bungeeSubchannel, String argument1, String argument2) {
            super(bungeeSubchannel, argument1, argument2);
        }

        @Override
        public String toString() {
            return getLeft().toString().toLowerCase() + " " + getMiddle() + " " + getRight();
        }
    }
}
