package org.samo_lego.taterzens.mixin;

import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerCommandSource.class)
public class ServerCommandSourceMixinCast_TaterzenEditor implements TaterzenEditor {
    @Unique
    private TaterzenNPC selectedNpc;
    @Unique
    private int taterzens$selectedMsgId = -1; // -1 as no selected msg to edit

    /**
     * Gets the selected {@link TaterzenNPC} if player has it.
     * @return TaterzenNPC if player has one selected, otherwise null.
     */
    @Nullable
    @Override
    public TaterzenNPC getNpc() {
        return this.selectedNpc;
    }

    @Override
    public void selectNpc(@Nullable TaterzenNPC npc) {
        this.selectedNpc = npc;
    }

    @Override
    public void setEditingMessageIndex(int selected) {
        this.taterzens$selectedMsgId = selected;
    }

    @Override
    public int getEditingMessageIndex() {
        return this.taterzens$selectedMsgId;
    }
}
