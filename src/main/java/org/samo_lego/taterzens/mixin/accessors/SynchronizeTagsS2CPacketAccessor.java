package org.samo_lego.taterzens.mixin.accessors;

import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.tag.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SynchronizeTagsS2CPacket.class)
public interface SynchronizeTagsS2CPacketAccessor {
    @Accessor("tagManager")
    TagManager getTagManager();

    @Mutable
    @Accessor("tagManager")
    void setTagManager(TagManager manager);
}
