package org.samo_lego.taterzens.mixin;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.samo_lego.taterzens.mixin.accessors.EntitySpawnS2CPacketAccessor;
import org.samo_lego.taterzens.mixin.accessors.MobSpawnS2CPacketAccessor;
import org.samo_lego.taterzens.mixin.accessors.PlayerListS2CPacketAccessor;
import org.samo_lego.taterzens.mixin.accessors.PlayerSpawnS2CPacketAccessor;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.ADD_PLAYER;
import static net.minecraft.util.registry.Registry.ENTITY_TYPE;

/**
 * Used to "fake" the TaterzenNPC entity type.
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_PacketFaker {

    @Shadow public ServerPlayerEntity player;

    @Shadow public abstract void sendPacket(Packet<?> packet);

    /**
     * Changes entity type if entity is an instance of {@link TaterzenNPC}.
     *
     * @param packet
     * @param listener
     * @param ci
     */
    @Inject(
            method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V"
            ),
            cancellable = true
    )
    private void changeEntityType(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if(packet instanceof MobSpawnS2CPacket) {
            World world = player.getEntityWorld();
            Entity entity = world.getEntityById(((MobSpawnS2CPacketAccessor) packet).getId());

            if(!(entity instanceof TaterzenNPC))
                return;



            TaterzenNPC npc = (TaterzenNPC) entity;
            if(npc.getFakeType() == EntityType.PLAYER) {
                PlayerListS2CPacket playerListS2CPacket = new PlayerListS2CPacket();
                //noinspection ConstantConditions
                PlayerListS2CPacketAccessor listS2CPacketAccessor = (PlayerListS2CPacketAccessor) playerListS2CPacket;
                listS2CPacketAccessor.setEntries(Collections.singletonList(playerListS2CPacket.new Entry(npc.getGameProfile(), 0, GameMode.SURVIVAL, npc.getName())));

                PlayerSpawnS2CPacket playerSpawnS2CPacket = new PlayerSpawnS2CPacket();
                //noinspection ConstantConditions
                PlayerSpawnS2CPacketAccessor spawnS2CPacketAccessor = (PlayerSpawnS2CPacketAccessor) playerSpawnS2CPacket;
                spawnS2CPacketAccessor.setId(npc.getEntityId());
                spawnS2CPacketAccessor.setUuid(npc.getUuid());
                spawnS2CPacketAccessor.setX(npc.getX());
                spawnS2CPacketAccessor.setY(npc.getY());
                spawnS2CPacketAccessor.setZ(npc.getZ());
                spawnS2CPacketAccessor.setYaw((byte)((int)(npc.yaw * 256.0F / 360.0F)));
                spawnS2CPacketAccessor.setPitch((byte)((int)(npc.pitch * 256.0F / 360.0F)));

                // Player needs to be added to list and then spawned
                // :mojank:
                listS2CPacketAccessor.setAction(ADD_PLAYER);
                this.sendPacket(playerListS2CPacket);

                // Spawning player
                this.sendPacket(playerSpawnS2CPacket);

                // And removing player from tablist
                //listS2CPacketAccessor.setAction(REMOVE_PLAYER);
                //this.sendPacket(playerListS2CPacket);
                ci.cancel();
            }
            else {
                int id = ENTITY_TYPE.getRawId(npc.getFakeType());
                if(npc.isFakeTypeAlive()) {
                    ((MobSpawnS2CPacketAccessor) packet).setEntityTypeId(id);
                }
                else {
                    EntitySpawnS2CPacket entitySpawnPacket = new EntitySpawnS2CPacket(npc);
                    //noinspection ConstantConditions
                    ((EntitySpawnS2CPacketAccessor) entitySpawnPacket).setEntityId(npc.getFakeType());
                    this.sendPacket(entitySpawnPacket);
                    ci.cancel();
                }
            }
            this.sendPacket(new EntitySetHeadYawS2CPacket(npc, (byte) ((int)npc.headYaw * 256.0F / 360.0F)));
        }
    }
}
