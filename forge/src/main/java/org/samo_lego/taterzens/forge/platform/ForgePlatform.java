package org.samo_lego.taterzens.forge.platform;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegistryManager;
import org.samo_lego.taterzens.platform.Platform;

import java.nio.file.Path;

public class ForgePlatform extends Platform {

    private static final ResourceLocation ITEM_ID = new ResourceLocation("item");

    @Override
    public Path getConfigDirPath() {
        return FMLPaths.CONFIGDIR.get();

    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public int getItemRegistrySize() {
        return RegistryManager.ACTIVE.getRegistry(ITEM_ID).getValues().size();
    }

    @Override
    public boolean checkPermission(CommandSourceStack source, String permissionNode, int fallbackLevel) {
        return source.hasPermission(fallbackLevel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerTaterzenType() {
        /*return (EntityType<TaterzenNPC>) EntityType.Builder
                .of(TaterzenNPC::new, MobCategory.MISC)
                .sized(0.6F, 1.8F)
                .build(NPC_ID.toString())
                .setRegistryName(NPC_ID.toString());*/
    }

    @Override
    public void openEditorGui(Player player) {
        player.sendMessage(new TranslatableComponent("taterzens.gui.forge"), player.getUUID());
    }
}
