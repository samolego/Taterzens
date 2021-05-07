package org.samo_lego.taterzens.compatibility.forge;

import net.minecraft.server.command.ServerCommandSource;

public class LoaderSpecificImpl {

    public static boolean permissions$checkPermission(ServerCommandSource source, String permissionNode, int fallbackLevel) {
        return source.hasPermissionLevel(fallbackLevel);
    }
}
