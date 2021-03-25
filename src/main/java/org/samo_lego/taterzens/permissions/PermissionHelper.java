package org.samo_lego.taterzens.permissions;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Permission checker.
 *
 * In its own class since we do not want to depend
 * on it fully, but use it just if luckperms mod is loaded.
 */
public class PermissionHelper {

    /**
     * Checks permission of commandSource using Lucko's
     * permission API.
     * If permission isn't set, it will require the commandSource
     * to have permission level of 4 (op).
     *
     * @param commandSource commandSource to check permission for.
     * @param permission permission node to check.
     * @return true if commandSource has the permission, otherwise false
     */
    public static boolean checkPermission(ServerCommandSource commandSource, String permission) {
        return Permissions.check(commandSource, permission, 4);
    }
}
