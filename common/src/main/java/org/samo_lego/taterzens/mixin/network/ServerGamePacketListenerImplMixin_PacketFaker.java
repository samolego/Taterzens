package org.samo_lego.taterzens.mixin.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.mixin.accessors.AClientboundAddPlayerPacket;
import org.samo_lego.taterzens.mixin.accessors.AClientboundPlayerInfoUpdatePacket;
import org.samo_lego.taterzens.mixin.accessors.AClientboundSetEntityDataPacket;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.samo_lego.taterzens.util.NpcPlayerUpdate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static org.samo_lego.taterzens.Taterzens.config;

/**
 * Used to "fake" the TaterzenNPC entity type.
 */
@Mixin(value = ServerGamePacketListenerImpl.class, priority = 900)
public abstract class ServerGamePacketListenerImplMixin_PacketFaker {

    @Unique
    private final Map<UUID, NpcPlayerUpdate> tablistQueue = new LinkedHashMap<>();
    @Shadow
    public ServerPlayer player;
    @Unique
    private boolean skipCheck = false;
    @Unique
    private int queueTick;

    @Shadow
    public abstract void send(Packet<?> packet, @Nullable PacketSendListener packetSendListener);

    @Shadow
    public abstract void send(Packet<?> packet);

    /**
     * Changes entity type if entity is an instance of {@link TaterzenNPC}.
     */
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"),
            cancellable = true)
    private void changeEntityType(Packet<?> packet, PacketSendListener listener, CallbackInfo ci) {
        Level world = player.level();
        if (packet instanceof BundlePacket<?> bPacket && !this.skipCheck) {
            for (Packet<?> subPacket : bPacket.subPackets()) {
                if (subPacket instanceof ClientboundAddPlayerPacket) {
                    Entity entity = world.getEntity(((AClientboundAddPlayerPacket) subPacket).getId());

                    if (!(entity instanceof TaterzenNPC npc)) return;

                    GameProfile profile = npc.getGameProfile();
                    //ClientboundPlayerInfoUpdatePacket playerAddPacket = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npc.getFakePlayer());
                    ClientboundPlayerInfoUpdatePacket playerAddPacket = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(Collections.singleton(npc.getFakePlayer()));
                    //noinspection ConstantConditions
                    var entry = new ClientboundPlayerInfoUpdatePacket.Entry(profile.getId(), profile, false, 0, GameType.SURVIVAL, npc.getDisplayName(), null);
                    ((AClientboundPlayerInfoUpdatePacket) playerAddPacket).setEntries(Collections.singletonList(entry));
                    this.send(playerAddPacket, listener);

                    // Before we send this packet, we have
                    // added player to tablist, otherwise client doesn't
                    // show it ... :mojank:
                    this.skipCheck = true;
                    this.send(packet, listener);
                    this.skipCheck = false;

                    // And now we can remove it from tablist
                    // we must delay the tablist packet so as to allow
                    // the client to fetch skin.
                    // If player is immediately removed from the tablist,
                    // client doesn't care about the skin.
                    if (config.taterzenTablistTimeout != -1) {
                        var uuid = npc.getGameProfile().getId();
                        tablistQueue.remove(uuid);
                        tablistQueue.put(uuid, new NpcPlayerUpdate(npc.getGameProfile(), npc.getTabListName(), queueTick + config.taterzenTablistTimeout));
                    }

                    this.send(new ClientboundRotateHeadPacket(entity, (byte) ((int) (entity.getYHeadRot() * 256.0F / 360.0F))), listener);

                    ci.cancel();
                } else if (subPacket instanceof ClientboundSetEntityDataPacket) {
                    Entity entity = world.getEntity(((AClientboundSetEntityDataPacket) subPacket).getEntityId());

                    if (!(entity instanceof TaterzenNPC taterzen)) return;
                    Player fakePlayer = taterzen.getFakePlayer();
                    List<SynchedEntityData.DataValue<?>> trackedValues = fakePlayer.getEntityData().getNonDefaultValues();

                    if (taterzen.equals(((ITaterzenEditor) this.player).getNpc()) && trackedValues != null && config.glowSelectedNpc) {
                        trackedValues.removeIf(value -> value.id() == 0);
                        byte flags = fakePlayer.getEntityData().get(Entity.DATA_SHARED_FLAGS_ID);
                        // Modify Taterzen to have fake glowing effect for the player
                        flags = (byte) (flags | 1 << Entity.FLAG_GLOWING);

                        SynchedEntityData.DataValue<Byte> glowingTag = SynchedEntityData.DataValue.create(Entity.DATA_SHARED_FLAGS_ID, flags);
                        trackedValues.add(glowingTag);
                    }

                    ((AClientboundSetEntityDataPacket) subPacket).setPackedItems(trackedValues);
                }
            }
        }
    }

    @Inject(method = "handleMovePlayer", at = @At("RETURN"))
    private void removeTaterzenFromTablist(CallbackInfo ci) {
        if (tablistQueue.isEmpty()) return;

        queueTick++;

        List<UUID> toRemove = new ArrayList<>();
        for (var iterator = tablistQueue.values().iterator(); iterator.hasNext(); ) {
            var current = iterator.next();
            if (current.removeAt() > queueTick) break;

            iterator.remove();
            toRemove.add(current.profile().getId());
        }
        if (toRemove.isEmpty()) return;

        ClientboundPlayerInfoRemovePacket taterzensRemovePacket = new ClientboundPlayerInfoRemovePacket(toRemove);

        this.skipCheck = true;
        this.send(taterzensRemovePacket);
        this.skipCheck = false;
    }
}
