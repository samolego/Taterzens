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
import org.samo_lego.taterzens.compatibility.ModDiscovery;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.samo_lego.taterzens.platform.Platform;
import org.samo_lego.taterzens.storage.TaterConfig;
import org.samo_lego.taterzens.util.LanguageUtil;
import org.samo_lego.taterzens.util.PermissionExtractor;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.samo_lego.taterzens.compatibility.ModDiscovery.LUCKPERMS_LOADED;

public class Taterzens {

    public static final String MOD_ID = "taterzens";
    public static final Logger LOGGER = (Logger) LogManager.getLogger(MOD_ID);
    private static Taterzens INSTANCE;

    /**
     * Configuration file.
     */
    public static TaterConfig config;

    /**
     * Language file.
     */
    public static JsonObject lang;

    /**
     * List of **loaded** {@link TaterzenNPC TaterzenNPCs}.
     */
    public static final Map<UUID, TaterzenNPC> TATERZEN_NPCS = Collections.synchronizedMap(new LinkedHashMap<>());

    public static final HashMap<ResourceLocation, Function<TaterzenNPC, TaterzenProfession>> PROFESSION_TYPES = new HashMap<>();
    public static final Gson GSON;

    /**
     * Taterzen entity type. Used server - only, as it is replaced with vanilla type
     * when packets are sent.
     */
    public static Supplier<EntityType<TaterzenNPC>> TATERZEN_TYPE;
    public static final ResourceLocation NPC_ID = new ResourceLocation(MOD_ID, "npc");

    private final File configFile;


    /**
     * Directory of the presets.
     */
    public final File presetsDir;
    private final Platform platform;

    public Taterzens(Platform platform) {
        INSTANCE = this;
        platform.registerTaterzenType();

        ModDiscovery.checkLoadedMods(platform);

        this.presetsDir = new File(platform.getConfigDirPath() + "/Taterzens/presets");
        this.platform = platform;

        if (!presetsDir.exists() && !presetsDir.mkdirs())
            throw new RuntimeException(String.format("[%s] Error creating directory!", MOD_ID));

        File taterDir = presetsDir.getParentFile();
        configFile = new File(taterDir + "/config.json");
        config = TaterConfig.loadConfigFile(configFile);

        LanguageUtil.setupLanguage();

        if(LUCKPERMS_LOADED && config.perms.savePermsFile) {
            PermissionExtractor.extractPermissionsFile(new File(taterDir + "/permissions.toml"));
        }
    }

    /**
     * Returns the presets directory.
     * @return presets directory.
     */
    public File getPresetDirectory() {
        return this.presetsDir;
    }

    /**
     * Gets the instance of the mod.
     * @return instance of the mod.
     */
    public static Taterzens getInstance() {
        return INSTANCE;
    }

    /**
     * Gets configuration file.
     * @return configuration file.
     */
    public File getConfigFile() {
        return this.configFile;
    }

    /**
     * Gets the platform - usable with loader-specific methods.
     * @return platform.
     */
    public Platform getPlatform() {
        return this.platform;
    }

    /**
     * Handles command registration.
     * @param dispatcher dispatcher to register commands to.
     */
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        NpcCommand.register(dispatcher);
        TaterzensCommand.register(dispatcher);
        NpcGUICommand.register(dispatcher);

        ProfessionCommand.register(dispatcher);
    }

    static {
        GSON = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .disableHtmlEscaping()
                .create();
    }
}
