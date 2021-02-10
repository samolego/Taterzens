package org.samo_lego.taterzens;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.commands.TaterzensCommand;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.samo_lego.taterzens.storage.TaterConfig;
import org.samo_lego.taterzens.storage.TaterLang;

import java.io.File;
import java.util.ArrayList;

public class Taterzens implements ModInitializer {

    public static final String MODID = "taterzens";

    public static TaterConfig config;
    public static TaterLang lang;
    private static final Logger LOGGER = (Logger) LogManager.getLogger();
    public static final ArrayList<TaterzenNPC> TATERZEN_NPCS = new ArrayList<>();
    private static File taterDir;

    public static final EntityType<TaterzenNPC> TATERZEN = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(MODID, "npc"),
            FabricEntityTypeBuilder
                    .<TaterzenNPC>create(SpawnGroup.MONSTER, TaterzenNPC::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
                    .build()
    );

    @Override
    public void onInitialize() {
        // Hooray
        CommandRegistrationCallback.EVENT.register(TaterzensCommand::register);
        CommandRegistrationCallback.EVENT.register(NpcCommand::register);

        FabricDefaultAttributeRegistry.register(TATERZEN, TaterzenNPC.createMobAttributes());

        taterDir = new File(FabricLoader.getInstance().getConfigDir() + "/Taterzens/presets");
        if (!taterDir.exists() && !taterDir.mkdirs())
            throw new RuntimeException(String.format("[%s] Error creating directory!", MODID));
        taterDir = taterDir.getParentFile();

        config = TaterConfig.loadConfigFile(new File(taterDir + "/config.json"));
        lang = TaterLang.loadLanguageFile(new File(taterDir + "/" + config.language + ".json"));
    }

    public static Logger getLogger() {
        return LOGGER;
    }
    public static File getTaterDir() {
        return taterDir;
    }
}
