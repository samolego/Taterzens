package org.samo_lego.taterzens.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;

/**
 * An NPC which is queued to be removed from the tab list at a future point.
 *
 * @param profile     The game profile we're removing from the tab list.
 * @param displayName The NPC's display name.
 * @param removeAt    The tick at which this NPC should be removed.
 * @see org.samo_lego.taterzens.mixin.network.ServerGamePacketListenerImplMixin_PacketFaker
 */
public record NpcPlayerUpdate(GameProfile profile, Component displayName, long removeAt) {
}
