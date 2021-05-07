package org.samo_lego.taterzens.compatibility.fabric;

import net.minecraft.server.command.ServerCommandSource;

import static org.samo_lego.taterzens.Taterzens.LUCKPERMS_ENABLED;
import static org.samo_lego.taterzens.compatibility.PermissionHelper.checkPermission;

public class LoaderSpecificImpl {
    public static boolean permissions$checkPermission(ServerCommandSource source, String permissionNode, int fallbackLevel) {
        return LUCKPERMS_ENABLED ? checkPermission(source, permissionNode, fallbackLevel) : source.hasPermissionLevel(fallbackLevel);
    }
}
