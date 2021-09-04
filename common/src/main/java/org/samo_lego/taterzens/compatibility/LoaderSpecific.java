package org.samo_lego.taterzens.compatibility;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.commands.CommandSourceStack;

/**
 * Methods that are further redirected to forge / fabric implementations.
 */
public class LoaderSpecific {

    @ExpectPlatform
    public static boolean permissions$checkPermission(CommandSourceStack source, String permissionNode, int fallbackLevel) {
        throw new AssertionError();
    }
}
