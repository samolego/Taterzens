package org.samo_lego.taterzens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.samo_lego.taterzens.api.professions.TaterzenProfession;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.commands.NpcGUICommand;
import org.samo_lego.taterzens.commands.ProfessionCommand;
import org.samo_lego.taterzens.commands.TaterzensCommand;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.samo_lego.taterzens.storage.TaterConfig;
import org.samo_lego.taterzens.util.LanguageUtil;
import org.samo_lego.taterzens.util.PermissionExtractor;

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
    public static JsonObject lang;
    public static final Logger LOGGER = (Logger) LogManager.getLogger(MODID);
    /**
     * List of **loaded** {@link TaterzenNPC TaterzenNPCs}.
     */
    public static final LinkedHashSet<TaterzenNPC> TATERZEN_NPCS = new LinkedHashSet<>();

    public static final HashMap<ResourceLocation, TaterzenProfession> PROFESSION_TYPES = new HashMap<>();


    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    /**
     * Taterzen entity type. Used server - only, as it is replaced with vanilla type
     * when packets are sent.
     */
    public static EntityType<TaterzenNPC> TATERZEN_TYPE;
    public static ResourceLocation NPC_ID = new ResourceLocation(MODID, "npc");

    public static File taterDir;
    public static File presetsDir;
    public static File CONFIG_FILE;


    /**
     * Whether LuckPerms mod is loaded.
     * @see <a href="https://luckperms.net/">LuckPerms website</a>.
     */
    public static boolean LUCKPERMS_LOADED;

    public static boolean SERVER_TRANSLATIONS_LOADED;

    public static boolean FABRICTAILOR_LOADED;

    public static boolean CARPETMOD_LOADED;


    public static void onInitialize() {
        if (!taterDir.exists() && !taterDir.mkdirs())
            throw new RuntimeException(String.format("[%s] Error creating directory!", MODID));
        presetsDir = taterDir;
        taterDir = taterDir.getParentFile();
        CONFIG_FILE = new File(taterDir + "/config.json");
        config = TaterConfig.loadConfigFile(CONFIG_FILE);

        LanguageUtil.setupLanguage();

        if(LUCKPERMS_LOADED && config.perms.savePermsFile) {
            PermissionExtractor.extractPermissionsFile(new File(taterDir + "/permissions.toml"));
        }
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        NpcCommand.register(dispatcher, dedicated);
        TaterzensCommand.register(dispatcher, dedicated);
        NpcGUICommand.register(dispatcher, dedicated);

        ProfessionCommand.register(dispatcher, dedicated);
    }
}
