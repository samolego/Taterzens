package org.samo_lego.taterzens.compatibility.fabric;

import static org.samo_lego.taterzens.Taterzens.LUCKPERMS_LOADED;
import static org.samo_lego.taterzens.compatibility.PermissionHelper.checkPermission;

import net.minecraft.commands.CommandSourceStack;

public class LoaderSpecificImpl {
    public static boolean permissions$checkPermission(CommandSourceStack source, String permissionNode, int fallbackLevel) {
        return LUCKPERMS_LOADED ? checkPermission(source, permissionNode, fallbackLevel) : source.hasPermission(fallbackLevel);
    }
}
