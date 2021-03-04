package org.samo_lego.taterzens.mixin;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.samo_lego.taterzens.mixin.accessors.EntityTrackerUpdateS2CPacketAccessor;
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
import java.util.List;

import static net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.ADD_PLAYER;
import static net.minecraft.util.registry.Registry.ENTITY_TYPE;
import static org.samo_lego.taterzens.Taterzens.TATERZEN;

/**
 * Used to "fake" the TaterzenNPC entity type.
 */
@Mixin(value = ServerPlayNetworkHandler.class, priority = 900)
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
        World world = player.getEntityWorld();
        if(packet instanceof MobSpawnS2CPacket) {

            Entity entity = world.getEntityById(((MobSpawnS2CPacketAccessor) packet).getId());

            if(!(entity instanceof TaterzenNPC) || ((MobSpawnS2CPacketAccessor) packet).getEntityTypeId() != ENTITY_TYPE.getRawId(TATERZEN))
                return;

            PlayerListS2CPacket playerListS2CPacket = new PlayerListS2CPacket();
            //noinspection ConstantConditions
            PlayerListS2CPacketAccessor listS2CPacketAccessor = (PlayerListS2CPacketAccessor) playerListS2CPacket;

            TaterzenNPC npc = (TaterzenNPC) entity;
            listS2CPacketAccessor.setAction(ADD_PLAYER);
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

            this.sendPacket(playerListS2CPacket);
            this.sendPacket(playerSpawnS2CPacket);
            this.sendPacket(new EntitySetHeadYawS2CPacket(entity, (byte)((int)(entity.getHeadYaw() * 256.0F / 360.0F))));
            ci.cancel();
        } else if(packet instanceof EntityTrackerUpdateS2CPacket) {
            Entity entity = world.getEntityById(((EntityTrackerUpdateS2CPacketAccessor) packet).getEntityId());

            if(!(entity instanceof TaterzenNPC))
                return;
            // Only change the content if entity is disguised
            PlayerEntity fakePlayer = ((TaterzenNPC) entity).getFakePlayer();
            if(fakePlayer != null) {
                List<DataTracker.Entry<?>> trackedValues = fakePlayer.getDataTracker().getAllEntries();
                ((EntityTrackerUpdateS2CPacketAccessor) packet).setTrackedValues(trackedValues);
            }
        }
    }
}
