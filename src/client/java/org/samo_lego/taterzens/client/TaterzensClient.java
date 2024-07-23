package org.samo_lego.taterzens.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.samo_lego.taterzens.common.Taterzens;
import org.samo_lego.taterzens.common.npc.TaterzenNPC;

public class TaterzensClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// We just do this to avoid client crashes
		/*
		EntityRendererRegistry.register(Taterzens.TATERZEN_TYPE.get(), context -> new MobRenderer<>(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 1.0F) {
			@Override
			public ResourceLocation getTextureLocation(TaterzenNPC entity) {
				return DefaultPlayerSkin.getDefaultTexture();
			}
		});
		*/
	}
}
