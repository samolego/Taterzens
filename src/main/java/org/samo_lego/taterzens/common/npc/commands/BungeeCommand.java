package org.samo_lego.taterzens.common.npc.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import org.samo_lego.taterzens.common.npc.TaterzenNPC;

import java.util.ArrayList;
import java.util.Locale;

import static org.samo_lego.taterzens.common.Taterzens.config;

/*

    Query as to viability of Bungee Server support.  Is this a function that still needs to be implemented in the mod?
    If not, we can remove Bungee Command and Server code and solve some 1.21 implemntation issues quickly.

*/
public class BungeeCommand extends AbstractTaterzenCommand {
    /**
     * Contains all available proxy servers.
     */
    public static final ArrayList<String> AVAILABLE_SERVERS = new ArrayList<>();

    /**
     * Identifier of the proxy message channel.
     */
    public static final ResourceLocation BUNGEE_CHANNEL = ResourceLocation.fromNamespaceAndPath("bungeecord", "main");
    private String argument;
    private BungeeMessage proxyMessage;
    private String playername;

    /**
     * "Bungee" command. It's not a standard command, executed by the server,
     * but sent as {@link ClientboundCustomPayloadPacket} to player and caught by proxy.
     *
     * @param proxyMessage proxy command to add, see {@link BungeeMessage} for supported commands.
     * @param playername   player to use when executing the command.
     * @param argument     argument for command above.
     * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#wikiPage">Spigot thread</a> on message channels.
     * @see <a href="https://github.com/VelocityPowered/Velocity/blob/65db0fad6a221205ec001f1f68a032215da402d6/proxy/src/main/java/com/velocitypowered/proxy/connection/backend/BungeeCordMessageResponder.java#L297">Proxy implementation</a> on GitHub.
     */
    public BungeeCommand(BungeeMessage proxyMessage, String playername, String argument) {
        super(CommandType.BUNGEE);
        this.proxyMessage = proxyMessage;
        this.playername = playername;
        this.argument = argument;
    }

    public BungeeCommand() {
        super(CommandType.BUNGEE);
        this.proxyMessage = null;
        this.playername = "";
        this.argument = "";
    }

    /**
     * Sends a packet to proxy.
     *
     * @param connection connection to use for sending packet.
     * @param data       data to sent in the packet.
     */
    public static void sendProxyPacket(ServerGamePacketListenerImpl connection, byte[] data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeResourceLocation(BUNGEE_CHANNEL);
        buf.writeBytes(data);

        // TODO: If we're keeping Bungee support, fix this

        // ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(buf);
        // connection.send(packet);
    }

    @Override
    public String toString() {
        return this.proxyMessage.toString().toLowerCase(Locale.ROOT) + " " + this.playername + " " + this.argument;
    }


    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag).putString("Proxy", this.proxyMessage.toString());
        tag.putString("Player", this.playername);
        tag.putString("Argument", this.argument);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag cmdTag) {
        this.proxyMessage = BungeeMessage.valueOf(cmdTag.getString("Proxy"));
        this.playername = cmdTag.getString("Player");
        this.argument = cmdTag.getString("Argument");
    }

    @Override
    public void execute(TaterzenNPC npc, Player player) {
        if (!config.bungee.enableCommands) return;
        // Sending command as CustomPayloadS2CPacket
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(this.proxyMessage.getSubchannel());
        out.writeUTF(this.playername.replace(CLICKER_PLACEHOLDER, player.getGameProfile().getName()));
        out.writeUTF(this.argument.replaceAll(CLICKER_PLACEHOLDER, player.getGameProfile().getName()));

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeResourceLocation(BUNGEE_CHANNEL);
        buf.writeBytes(out.toByteArray());

        // TODO: If we're keeping Bungee support, fix this

        //ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(buf);
        //((ServerPlayer) player).connection.send(packet);
    }


    /**
     * Available / supported bungee commands.
     *
     * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#wikiPage">Spigot thread</a> on message channels.
     * @see <a href="https://github.com/VelocityPowered/Velocity/blob/65db0fad6a221205ec001f1f68a032215da402d6/proxy/src/main/java/com/velocitypowered/proxy/connection/backend/BungeeCordMessageResponder.java#L297">Proxy implementation</a> on GitHub.
     */
    public enum BungeeMessage {
        SERVER("ConnectOther"),
        MESSAGE("Message"),
        MESSAGE_RAW("MessageRaw"),
        KICK("KickPlayer");


        private final String subchannel;

        BungeeMessage(String subchannel) {
            this.subchannel = subchannel;
        }

        /**
         * Gets the subchannel of enum.
         *
         * @return proxy subchannel.
         */
        public String getSubchannel() {
            return this.subchannel;
        }
    }
}
