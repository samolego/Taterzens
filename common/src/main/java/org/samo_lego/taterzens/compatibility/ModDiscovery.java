package org.samo_lego.taterzens.compatibility;

import org.samo_lego.taterzens.platform.Platform;

public class ModDiscovery {

    /**
     * Whether LuckPerms mod is loaded.
     * @see <a href="https://luckperms.net/">LuckPerms website</a>.
     */
    public static boolean LUCKPERMS_LOADED;
    public static boolean DISGUISELIB_LOADED;
    public static boolean SERVER_TRANSLATIONS_LOADED;
    public static boolean FABRICTAILOR_LOADED;
    public static boolean CARPETMOD_LOADED;


    public static void checkLoadedMods(Platform platform) {
        // Loaded mods
        LUCKPERMS_LOADED = platform.isModLoaded("fabric-permissions-api-v0");
        DISGUISELIB_LOADED = platform.isModLoaded("disguiselib");
        SERVER_TRANSLATIONS_LOADED = platform.isModLoaded("server_translations_api");
        FABRICTAILOR_LOADED = platform.isModLoaded("fabrictailor");
        CARPETMOD_LOADED = platform.isModLoaded("carpet");
    }
}
