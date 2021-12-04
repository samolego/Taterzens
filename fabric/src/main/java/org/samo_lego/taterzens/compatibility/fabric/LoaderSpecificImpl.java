package org.samo_lego.taterzens.compatibility.fabric;

import net.minecraft.commands.CommandSourceStack;
import org.samo_lego.taterzens.fabric.mixin.MappedRegistryAccessor;

import static net.minecraft.core.Registry.ITEM;
import static org.samo_lego.taterzens.Taterzens.LUCKPERMS_LOADED;
import static org.samo_lego.taterzens.compatibility.PermissionHelper.checkPermission;

public class LoaderSpecificImpl {

    private static final int REGISTRY_ITEMS_SIZE = ((MappedRegistryAccessor) ITEM).getById().size();

    public static boolean permissions$checkPermission(CommandSourceStack source, String permissionNode, int fallbackLevel) {
        return LUCKPERMS_LOADED ? checkPermission(source, permissionNode, fallbackLevel) : source.hasPermission(fallbackLevel);
    }

    public static int getItemRegistrySize() {
        return REGISTRY_ITEMS_SIZE;
    }
}
