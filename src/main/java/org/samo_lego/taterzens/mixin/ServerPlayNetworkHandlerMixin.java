package org.samo_lego.taterzens.mixin;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.samo_lego.taterzens.mixin.accessors.*;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.ADD_PLAYER;
import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.REMOVE_PLAYER;

/**
 * Used to "fake" the TaterzenNPC entity type
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

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

            PlayerListS2CPacket playerListS2CPacket = new PlayerListS2CPacket();
            //noinspection ConstantConditions
            PlayerListS2CPacketAccessor listS2CPacketAccessor = (PlayerListS2CPacketAccessor) playerListS2CPacket;

            TaterzenNPC npc = (TaterzenNPC) entity;
            if(npc.getFakeType() == EntityType.PLAYER) {
                listS2CPacketAccessor.setAction(ADD_PLAYER);
                listS2CPacketAccessor.setEntries(Collections.singletonList(playerListS2CPacket.new Entry(npc.getGameProfile(), 0, GameMode.SURVIVAL, npc.getName())));

                PlayerSpawnS2CPacket playerSpawnS2CPacket = new PlayerSpawnS2CPacket();
                //noinspection ConstantConditions - Accessor
                PlayerSpawnS2CPacketAccessor spawnS2CPacketAccessor = (PlayerSpawnS2CPacketAccessor) playerSpawnS2CPacket;
                spawnS2CPacketAccessor.setId(npc.getEntityId());
                spawnS2CPacketAccessor.setUuid(npc.getUuid());
                spawnS2CPacketAccessor.setX(npc.getX());
                spawnS2CPacketAccessor.setY(npc.getY());
                spawnS2CPacketAccessor.setZ(npc.getZ());
                spawnS2CPacketAccessor.setYaw((byte)((int)(npc.yaw * 256.0F / 360.0F)));
                spawnS2CPacketAccessor.setPitch((byte)((int)(npc.pitch * 256.0F / 360.0F)));

                this.sendPacket(playerListS2CPacket);
                this.sendPacket(playerSpawnS2CPacket);
                ci.cancel();
            }
            else {
                // Removing player from client tab
                listS2CPacketAccessor.setAction(REMOVE_PLAYER);
                listS2CPacketAccessor.setEntries(Collections.singletonList(playerListS2CPacket.new Entry(npc.getGameProfile(), 0, GameMode.SURVIVAL, npc.getName())));
                this.sendPacket(playerListS2CPacket);

                int id = Registry.ENTITY_TYPE.getRawId(npc.getFakeType());
                if(npc.isFakeTypeAlive()) {
                    ((MobSpawnS2CPacketAccessor) packet).setEntityTypeId(id);
                }
                else {
                    EntitySpawnS2CPacket entitySpawnPacket = new EntitySpawnS2CPacket(npc);
                    //noinspection ConstantConditions
                    ((EntitySpawnS2CPacketAccessor) entitySpawnPacket).setEntityId(npc.getFakeType());
                    this.sendPacket(entitySpawnPacket); //todo
                    //this.sendPacket(new EntityVelocityUpdateS2CPacket(npc));
                    ci.cancel();
                }
            }
            this.sendPacket(new EntitySetHeadYawS2CPacket(npc, (byte) ((int)npc.headYaw * 256.0F / 360.0F)));
        }
        /*else if(packet instanceof EntityTrackerUpdateS2CPacket) {
            World world = player.getEntityWorld();
            EntityTrackerUpdateS2CPacketAccessor accessor = (EntityTrackerUpdateS2CPacketAccessor) packet;
            Entity entity = world.getEntityById(accessor.getId());

            if(!(entity instanceof TaterzenNPC))
                return;

            accessor.getTrackedValues().set(8, );

        }*/
    }
}
