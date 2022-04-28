package org.samo_lego.taterzens.platform;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

import java.nio.file.Path;

/**
 * Base class for platform implementations.
 */
public abstract class Platform {

    /**
     * Gets the path to the directory where the config files are stored.
     * @return the path to the directory where the config files are stored.
     */
    public abstract Path getConfigDirPath();

    /**
     * Checks whether certain mod is loaded.
     * @param modId the mod id.
     * @return true if the mod is loaded, false otherwise.
     */
    public abstract boolean isModLoaded(String modId);

    /**
     * Gets size of item registry.
     * @return size of item registry.
     */
    public abstract int getItemRegistrySize();

    /**
     * Checks for permission of provided command source.
     * @param source the command source to check permission for.
     * @param permissionNode the permission node to check.
     * @param fallbackLevel the fallback level to use if the permission node is not set.
     * @return true if the command source has the permission node, false otherwise.
     */
    public abstract boolean checkPermission(CommandSourceStack source, String permissionNode, int fallbackLevel);

    /**
     * Registers the taterzen entity type.
     * @return the taterzen entity type that was registered.
     */
    public abstract void registerTaterzenType();

    public void openEditorGui(Player player) {
    }
}
