package org.samo_lego.taterzens.client_fix;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.samo_lego.taterzens.Taterzens;

@Environment(EnvType.CLIENT)
public class ClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // We just do this to avoid client crashes
        EntityRendererRegistry.INSTANCE.register(Taterzens.TATERZEN, (dispatcher, context) -> new PlayerEntityRenderer(dispatcher));
    }
}
