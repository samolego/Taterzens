package org.samo_lego.taterzens.client_fix;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.npc.TaterzenNPC;

@Environment(EnvType.CLIENT)
public class ClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // We just do this to avoid client crashes
        EntityRendererRegistry.INSTANCE.register(Taterzens.TATERZEN_TYPE, (dispatcher, context) -> new LivingEntityRenderer<TaterzenNPC, PlayerEntityModel<TaterzenNPC>>(dispatcher, new PlayerEntityModel<>(0.0F, false), 1.0F) {
            @Override
            public Identifier getTexture(TaterzenNPC entity) {
                return DefaultSkinHelper.getTexture(entity.getUuid());
            }
        });
    }
}
