package org.samo_lego.taterzens.compatibility.forge;

import net.minecraft.commands.CommandSourceStack;

public class LoaderSpecificImpl {

    public static boolean permissions$checkPermission(CommandSourceStack source, String permissionNode, int fallbackLevel) {
        return source.hasPermission(fallbackLevel);
    }
}
