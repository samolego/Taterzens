package org.samo_lego.taterzens.mixin.network;

import com.mojang.authlib.GameProfile;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.samo_lego.taterzens.compatibility.DisguiseLibCompatibility;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.mixin.accessors.ClientboundAddPlayerPacketAccessor;
import org.samo_lego.taterzens.mixin.accessors.ClientboundPlayerInfoPacketAccessor;
import org.samo_lego.taterzens.mixin.accessors.ClientboundSetEntityDataPacketAccessor;
import org.samo_lego.taterzens.mixin.accessors.EntityAccessor;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.samo_lego.taterzens.util.NpcPlayerUpdate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action.ADD_PLAYER;
import static net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.ModDiscovery.DISGUISELIB_LOADED;

/**
 * Used to "fake" the TaterzenNPC entity type.
 */
@Mixin(value = ServerGamePacketListenerImpl.class, priority = 900)
public abstract class ServerGamePacketListenerImplMixin_PacketFaker {

    @Shadow public ServerPlayer player;

    @Final
    @Shadow
    public Connection connection;

    @Shadow public abstract void send(Packet<?> packet);

    @Unique
    private boolean taterzens$skipCheck;
    @Unique
    private final Map<UUID, NpcPlayerUpdate> taterzens$tablistQueue = new LinkedHashMap<>();
    @Unique
    private int taterzens$queueTick;

    /**
     * Changes entity type if entity is an instance of {@link TaterzenNPC}.
     *
     * @param packet packet to change
     * @param listener
     */
    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V"
            ),
            cancellable = true
    )
    private void changeEntityType(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        Level world = player.getLevel();
        if(packet instanceof ClientboundAddPlayerPacket && !this.taterzens$skipCheck) {
            Entity entity = world.getEntity(((ClientboundAddPlayerPacketAccessor) packet).getId());

            if(!(entity instanceof TaterzenNPC npc) || (DISGUISELIB_LOADED && DisguiseLibCompatibility.isDisguised(entity)))
                return;

            GameProfile profile = npc.getGameProfile();
            ClientboundPlayerInfoPacket playerAddPacket = new ClientboundPlayerInfoPacket(ADD_PLAYER);
            //noinspection ConstantConditions
            ((ClientboundPlayerInfoPacketAccessor) playerAddPacket).setEntries(
                Arrays.asList(new ClientboundPlayerInfoPacket.PlayerUpdate(profile, 0, GameType.SURVIVAL, npc.getTabListName()))
            );
            this.send(playerAddPacket);

            // Before we send this packet, we have
            // added player to tablist, otherwise client doesn't
            // show it ... :mojank:
            this.taterzens$skipCheck = true;
            this.send(packet);
            this.taterzens$skipCheck = false;

            // And now we can remove it from tablist
            // we must delay the tablist packet so as to allow
            // the client to fetch skin.
            // If player is immediately removed from the tablist,
            // client doesn't care about the skin.
            if(config.taterzenTablistTimeout != -1) {
                var uuid = npc.getGameProfile().getId();
                taterzens$tablistQueue.remove(uuid);
                taterzens$tablistQueue.put(uuid, new NpcPlayerUpdate(npc.getGameProfile(), npc.getTabListName(), taterzens$queueTick + config.taterzenTablistTimeout));
            }

            this.connection.send(new ClientboundRotateHeadPacket(entity, (byte)((int)(entity.getYHeadRot() * 256.0F / 360.0F))), listener);

            ci.cancel();
        } else if(packet instanceof ClientboundSetEntityDataPacket) {
            Entity entity = world.getEntity(((ClientboundSetEntityDataPacketAccessor) packet).getEntityId());

            if(!(entity instanceof TaterzenNPC taterzen))
                return;
            Player fakePlayer = taterzen.getFakePlayer();
            List<SynchedEntityData.DataItem<?>> trackedValues = fakePlayer.getEntityData().getAll();

            if(taterzen.equals(((ITaterzenEditor) this.player).getNpc()) && trackedValues != null) {
                trackedValues.removeIf(value -> value.getAccessor().getId() == 0);
                Byte flags = fakePlayer.getEntityData().get(EntityAccessor.getFLAGS());
                // Modify Taterzen to have fake glowing effect for the player
                flags = (byte)(flags | 1 << EntityAccessor.getFLAG_GLOWING());

                SynchedEntityData.DataItem<Byte> glowingTag = new SynchedEntityData.DataItem<>(EntityAccessor.getFLAGS(), flags);
                trackedValues.add(glowingTag);
            }

            ((ClientboundSetEntityDataPacketAccessor) packet).setPackedItems(trackedValues);
        } else if(packet instanceof ClientboundPlayerInfoPacket && !this.taterzens$skipCheck) {
            ((ClientboundPlayerInfoPacketAccessor) packet).getEntries().forEach(entry -> {
                if(entry.getProfile().getName().equals("-" + config.defaults.name + "-")) {
                    // Fixes unloaded taterzens showing in tablist (disguiselib)
                    var uuid = entry.getProfile().getId();
                    taterzens$tablistQueue.remove(uuid);
                    taterzens$tablistQueue.put(uuid, new NpcPlayerUpdate(entry.getProfile(), entry.getDisplayName(), taterzens$queueTick + config.taterzenTablistTimeout));
                }
            });
        }
    }

    @Inject(method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V", at = @At("RETURN"))
    private void removeTaterzenFromTablist(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if(taterzens$tablistQueue.isEmpty()) return;

        taterzens$queueTick++;

        List<ClientboundPlayerInfoPacket.PlayerUpdate> toRemove = new ArrayList<>();
        for(var iterator = taterzens$tablistQueue.values().iterator(); iterator.hasNext(); ) {
            var current = iterator.next();
            if(current.removeAt() > taterzens$queueTick) break;

            iterator.remove();
            toRemove.add(new ClientboundPlayerInfoPacket.PlayerUpdate(current.profile(), 0, GameType.SURVIVAL, current.displayName()));
        }
        if(toRemove.isEmpty()) return;

        ClientboundPlayerInfoPacket taterzensRemovePacket = new ClientboundPlayerInfoPacket(REMOVE_PLAYER);
        //noinspection ConstantConditions
        ((ClientboundPlayerInfoPacketAccessor) taterzensRemovePacket).setEntries(toRemove);

        this.taterzens$skipCheck = true;
        this.send(taterzensRemovePacket);
        this.taterzens$skipCheck = false;
    }
}
