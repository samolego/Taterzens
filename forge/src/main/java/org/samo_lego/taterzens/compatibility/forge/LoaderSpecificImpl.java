package org.samo_lego.taterzens.compatibility.forge;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryManager;

public class LoaderSpecificImpl {

    private static final ResourceLocation ITEM_ID = new ResourceLocation("item");

    public static boolean permissions$checkPermission(CommandSourceStack source, String permissionNode, int fallbackLevel) {
        return source.hasPermission(fallbackLevel);
    }

    public static int getItemRegistrySize() {
        return RegistryManager.ACTIVE.getRegistry(ITEM_ID).getValues().size();
    }
}
