package org.samo_lego.taterzens.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.samo_lego.taterzens.common.api.professions.TaterzenProfession;
import org.samo_lego.taterzens.common.commands.NpcCommand;
import org.samo_lego.taterzens.common.commands.NpcGUICommand;
import org.samo_lego.taterzens.common.commands.ProfessionCommand;
import org.samo_lego.taterzens.common.compatibility.ModDiscovery;
import org.samo_lego.taterzens.common.npc.TaterzenNPC;
import org.samo_lego.taterzens.common.platform.Platform;
import org.samo_lego.taterzens.common.storage.TaterConfig;
import org.samo_lego.taterzens.common.util.LanguageUtil;
import org.samo_lego.taterzens.common.commands.TaterzensCommand;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

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
    public static JsonObject lang = new JsonObject();

    /**
     * List of **loaded** {@link TaterzenNPC TaterzenNPCs}.
     */
    public static final Map<UUID, TaterzenNPC> TATERZEN_NPCS = Collections.synchronizedMap(new LinkedHashMap<>());

    public static final Map<ResourceLocation, Function<TaterzenNPC, TaterzenProfession>> PROFESSION_TYPES = new HashMap<>();
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

        if (!presetsDir.exists() && !presetsDir.mkdirs()) {
            throw new RuntimeException(String.format("[%s] Error creating directory!", MOD_ID));
        }

        File taterDir = presetsDir.getParentFile();
        configFile = new File(taterDir + "/config.json");
        config = TaterConfig.loadConfigFile(configFile);

        LanguageUtil.setupLanguage();

        PolymerEntityUtils.registerType(TATERZEN_TYPE.get());
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
     *
     * @param dispatcher dispatcher to register commands to.
     * @param context
     */
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        NpcCommand.register(dispatcher, context);
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
