package org.samo_lego.taterzens.mixin.network;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
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

import java.util.*;

import static org.samo_lego.taterzens.Taterzens.config;

/**
 * Used to "fake" the TaterzenNPC entity type.
 */
@Mixin(value = ServerGamePacketListenerImpl.class, priority = 900)
public abstract class ServerGamePacketListenerImplMixin_PacketFaker {

    @Shadow
    public ServerPlayer player;

    @Final
    @Shadow
    private Connection connection;

    @Shadow
    public abstract void send(Packet<?> packet, @Nullable PacketSendListener packetSendListener);

    @Shadow
    public abstract void send(Packet<?> packet);

    @Unique
    private boolean taterzens$skipCheck;
    @Unique
    private final Map<UUID, NpcPlayerUpdate> taterzens$tablistQueue = new LinkedHashMap<>();
    @Unique
    private int taterzens$queueTick;

    /**
     * Changes entity type if entity is an instance of {@link TaterzenNPC}.
     */
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"),
            cancellable = true)
    private void changeEntityType(Packet<?> packet, PacketSendListener listener, CallbackInfo ci) {
        Level world = player.getLevel();
        if (packet instanceof BundlePacket<?> && !this.taterzens$skipCheck) {
            System.out.println("BundlePacket, todo - remove from tablist");
            /*if (!(entity instanceof TaterzenNPC npc))
                return;

            // And now we can remove it from tablist
            // we must delay the tablist packet so as to allow
            // the client to fetch skin.
            // If player is immediately removed from the tablist,
            // client doesn't care about the skin.
            if (config.taterzenTablistTimeout != -1) {
                var uuid = npc.getGameProfile().getId();
                taterzens$tablistQueue.remove(uuid);
                taterzens$tablistQueue.put(uuid, new NpcPlayerUpdate(npc.getGameProfile(), npc.getTabListName(), taterzens$queueTick + config.taterzenTablistTimeout));
            }*/
        } else if (packet instanceof ClientboundSetEntityDataPacket) {
            Entity entity = world.getEntity(((ClientboundSetEntityDataPacketAccessor) packet).getEntityId());

            if (!(entity instanceof TaterzenNPC taterzen))
                return;
            Player fakePlayer = taterzen.getFakePlayer();
            List<SynchedEntityData.DataValue<?>> trackedValues = fakePlayer.getEntityData().getNonDefaultValues();

            if (taterzen.equals(((ITaterzenEditor) this.player).getNpc()) && trackedValues != null && config.glowSelectedNpc) {
                trackedValues.removeIf(value -> value.id() == 0);
                Byte flags = fakePlayer.getEntityData().get(EntityAccessor.getFLAGS());
                // Modify Taterzen to have fake glowing effect for the player
                flags = (byte) (flags | 1 << EntityAccessor.getFLAG_GLOWING());

                SynchedEntityData.DataValue<Byte> glowingTag = SynchedEntityData.DataValue.create(EntityAccessor.getFLAGS(), flags);
                trackedValues.add(glowingTag);
            }

            ((ClientboundSetEntityDataPacketAccessor) packet).setPackedItems(trackedValues);
        }
    }

    @Inject(method = "handleMovePlayer", at = @At("RETURN"))
    private void removeTaterzenFromTablist(CallbackInfo ci) {
        if (taterzens$tablistQueue.isEmpty()) return;

        taterzens$queueTick++;

        List<UUID> toRemove = new ArrayList<>();
        for (var iterator = taterzens$tablistQueue.values().iterator(); iterator.hasNext(); ) {
            var current = iterator.next();
            if (current.removeAt() > taterzens$queueTick) break;

            iterator.remove();
            toRemove.add(current.profile().getId());
        }
        if (toRemove.isEmpty()) return;

        ClientboundPlayerInfoRemovePacket taterzensRemovePacket = new ClientboundPlayerInfoRemovePacket(toRemove);

        this.taterzens$skipCheck = true;
        this.send(taterzensRemovePacket);
        this.taterzens$skipCheck = false;
    }
}
