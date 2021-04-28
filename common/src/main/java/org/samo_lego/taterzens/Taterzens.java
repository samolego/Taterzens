package org.samo_lego.taterzens;

import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.samo_lego.taterzens.api.professions.TaterzenProfession;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.samo_lego.taterzens.storage.PermissionList;
import org.samo_lego.taterzens.storage.TaterConfig;
import org.samo_lego.taterzens.storage.TaterLang;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class Taterzens {

    public static final String MODID = "taterzens";
    public static boolean DISGUISELIB_LOADED;

    /**
     * Configuration file.
     */
    public static TaterConfig config;
    /**
     * Language file.
     */
    public static TaterLang lang;
    public static final Logger LOGGER = (Logger) LogManager.getLogger(MODID);
    /**
     * List of **loaded** {@link TaterzenNPC TaterzenNPCs}.
     */
    public static final LinkedHashSet<TaterzenNPC> TATERZEN_NPCS = new LinkedHashSet<>();

    public static final HashMap<Identifier, TaterzenProfession> PROFESSION_TYPES = new HashMap<>();

    /**
     * Taterzen entity type. Used server - only, as it is replaced with vanilla type
     * when packets are sent.
     */
    public static EntityType<TaterzenNPC> TATERZEN_TYPE;

    /**
     * Permissions for players.
     * Used only if LuckPerms mod is loaded.
     * @see Taterzens#LUCKPERMS_ENABLED
     */
    public static final PermissionList PERMISSIONS = new PermissionList();

    public static File taterDir;
    public static File presetsDir;


    /**
     * Whether LuckPerms mod is loaded.
     * @see <a href="https://luckperms.net/">LuckPerms website</a>.
     */
    public static boolean LUCKPERMS_ENABLED;

    public static boolean FABRICTAILOR_LOADED;

    public static void onInitialize() {
        if (!taterDir.exists() && !taterDir.mkdirs())
            throw new RuntimeException(String.format("[%s] Error creating directory!", MODID));
        presetsDir = taterDir;
        taterDir = taterDir.getParentFile();

        config = TaterConfig.loadConfigFile(new File(taterDir + "/config.json"));
        lang = TaterLang.loadLanguageFile(new File(taterDir + "/" + config.language + ".json"));

        if(LUCKPERMS_ENABLED) {
            PERMISSIONS.savePermissionList(new File(taterDir + "/permissions.json"));
        }
    }
}
