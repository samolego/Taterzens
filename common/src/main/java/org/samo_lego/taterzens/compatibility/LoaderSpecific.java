package org.samo_lego.taterzens.compatibility;

import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Methods that are further redirected to forge / fabric implementations.
 */
public class LoaderSpecific {

    @ExpectPlatform
    public static boolean permissions$checkPermission(ServerCommandSource source, String permissionNode, int fallbackLevel) {
        throw new AssertionError();
    }
}
