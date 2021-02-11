package org.samo_lego.taterzens.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixinCast_TaterzenEditor implements TaterzenEditor {
    @Unique
    private TaterzenNPC selectedNpc;
    @Unique
    private boolean inEditMode;

    @Override
    public TaterzenNPC getNpc() {
        return this.selectedNpc;
    }

    @Override
    public void selectNpc(TaterzenNPC npc) {
        this.selectedNpc = npc;
    }

    @Override
    public boolean inPathEditMode() {
        return this.inEditMode;
    }

    @Override
    public void setPathEditMode(boolean editMode) {
        this.inEditMode = editMode;
    }
}
