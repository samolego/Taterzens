package org.samo_lego.taterzens.fabric.client_fix;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.npc.TaterzenNPC;

@Environment(EnvType.CLIENT)
public class ClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // We just do this to avoid client crashes
        EntityRendererRegistry.register(Taterzens.TATERZEN_TYPE, context -> new MobRenderer<>(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 1.0F) {
            @Override
            public ResourceLocation getTextureLocation(TaterzenNPC entity) {
                return DefaultPlayerSkin.getDefaultSkin(entity.getUUID());
            }
        });
    }
}
