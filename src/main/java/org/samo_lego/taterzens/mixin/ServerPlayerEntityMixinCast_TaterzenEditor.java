package org.samo_lego.taterzens.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixinCast_TaterzenEditor implements TaterzenEditor {
    private TaterzenNPC selectedNpc;

    @Override
    public TaterzenNPC getNpc() {
        return this.selectedNpc;
    }

    @Override
    public void selectNpc(TaterzenNPC npc) {
        this.selectedNpc = npc;
    }
}
