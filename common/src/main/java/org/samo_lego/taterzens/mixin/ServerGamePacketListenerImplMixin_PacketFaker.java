package org.samo_lego.taterzens.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.mixin.accessors.ClientboundAddPlayerPacketAccessor;
import org.samo_lego.taterzens.mixin.accessors.ClientboundPlayerInfoPacketAccessor;
import org.samo_lego.taterzens.mixin.accessors.ClientboundSetEntityDataPacketAccessor;
import org.samo_lego.taterzens.mixin.accessors.EntityAccessor;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action.ADD_PLAYER;
import static net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER;
import static org.samo_lego.taterzens.Taterzens.config;

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
    private final List<Pair<GameProfile, Component>> taterzens$tablistQueue = new ArrayList<>();
    @Unique
    private int taterzens$queueTimer;

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

            if(!(entity instanceof TaterzenNPC npc))
                return;

            GameProfile profile = npc.getGameProfile();

            ClientboundPlayerInfoPacket playerAddPacket = new ClientboundPlayerInfoPacket(ADD_PLAYER);
            //noinspection ConstantConditions
            ((ClientboundPlayerInfoPacketAccessor) playerAddPacket).setEntries(
                    Arrays.asList(new ClientboundPlayerInfoPacket.PlayerUpdate(profile, 0, GameType.SURVIVAL, npc.getName()))
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
            this.taterzens$queueTimer = config.taterzenTablistTimeout;
            if(this.taterzens$queueTimer != -1)
                this.taterzens$tablistQueue.add(new Pair<>(npc.getGameProfile(), npc.getName()));

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
            this.taterzens$skipCheck = true;

            this.taterzens$queueTimer = config.taterzenTablistTimeout;
            ((ClientboundPlayerInfoPacketAccessor) packet).getEntries().forEach(entry -> {
                if(entry.getProfile().getName().equals("-" + config.defaults.name + "-")) {
                    // Fixes unloaded taterzens showing in tablist (disguiselib)
                    this.taterzens$tablistQueue.add(new Pair<>(entry.getProfile(), entry.getDisplayName()));
                }
            });

            this.taterzens$skipCheck = false;
        }
    }

    @Inject(method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V", at = @At("RETURN"))
    private void removeTaterzenFromTablist(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if(!this.taterzens$tablistQueue.isEmpty() && --this.taterzens$queueTimer <= 0) {
            this.taterzens$skipCheck = true;

            ClientboundPlayerInfoPacket taterzensRemovePacket = new ClientboundPlayerInfoPacket(REMOVE_PLAYER);
            List<ClientboundPlayerInfoPacket.PlayerUpdate> taterzenList = this.taterzens$tablistQueue
                    .stream()
                    .map(pair -> new ClientboundPlayerInfoPacket.PlayerUpdate(
                                    pair.getFirst(),
                                    0,
                                    GameType.SURVIVAL,
                                    pair.getSecond()
                            )
                    )
                    .collect(Collectors.toList());
            //noinspection ConstantConditions
            ((ClientboundPlayerInfoPacketAccessor) taterzensRemovePacket).setEntries(taterzenList);
            this.send(taterzensRemovePacket);

            this.taterzens$tablistQueue.clear();

            this.taterzens$skipCheck = false;
        }
    }
}
