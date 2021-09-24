package org.samo_lego.taterzens.event;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class PlayerJoinEventImpl implements ServerPlayConnectionEvents.Join {

    @Override
    public void onPlayReady(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        PlayerJoinEvent.onPlayerJoin(handler);
    }
}
