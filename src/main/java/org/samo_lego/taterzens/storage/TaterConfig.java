package org.samo_lego.taterzens.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.samo_lego.taterzens.Taterzens.MODID;
import static org.samo_lego.taterzens.Taterzens.getLogger;

public class TaterConfig {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    public String _comment_language = "// Language file to use.";
    /**
     * Language file used by Taterzens.
     *
     * Located at $minecraftFolder/config/Taterzens/$lang.json
     */
    public String language = "en_us";

    public String _comment_disableRegistrySync = "// Whether to disable Fabric's registry sync. Leave this to true if you'd like to keep the mod server-sided.";
    @SerializedName("disable_registry_sync")
    public boolean disableRegistrySync = true;


    public String _comment_fabricTailorAdvert = "// Whether to remind you that if FabricTailor mod is installed, it has some more skin functionality for Taterzens as well.";
    /**
     * Whether to remind you that if FabricTailor
     * mod is installed, it has some more skin functionality.
     *
     * @see <a href="https://github.com/samolego/FabricTailor">FabricTailor</a>
     */
    @SerializedName("post_fabrictailor_advert")
    public boolean fabricTailorAdvert = true;

    public Defaults defaults = new Defaults();
    public Path path = new Path();
    public Messages messages = new Messages();

    /**
     * Default {@link org.samo_lego.taterzens.npc.TaterzenNPC} settings.
     */
    public static class Defaults {
        public String _comment_name = "// Default settings for new Taterzens.";
        public String name = "Taterzen";
        public boolean leashable = false;
        public boolean pushable = false;

        public String _comment_commandPermissionLevel = "// Default command permission level of Taterzen";
        @SerializedName("command_permission_level")
        public int commandPermissionLevel = 4;

        public String _comment_sounds = "// Default sounds for Taterzens. Set to null to mute them.";
        @SerializedName("death_sound")
        public String deathSound = "entity.player.death";
        @SerializedName("hurt_sound")
        public String hurtSound = "entity.player.hurt";
        @SerializedName("ambient_sound")
        public String ambientSound = "entity.player.breath";
    }

    public static class Messages {
        public String _comment_messageDelay = "// Default delay between each message, in ticks.";
        public int messageDelay = 100;
        public String _comment_exitEditorAfterMsgEdit = "// Whether to exit message editor mode after editing a message.";
        @SerializedName("exit_editor_after_msg_edit")
        public boolean exitEditorAfterMsgEdit = true;
    }


    public static class Path {
        public Path.Color color = new Path.Color();
        /**
         * Color of particles used in path editor.
         */
        public static class Color {
            public String _comment = "// Which color of particles to use in path editor. Use RGB values ( 0 - 255 ).";
            public float red = 0;
            public float green = 255 ;
            public float blue = 255;
        }
    }


    /**
     * Loads language file.
     *
     * @param file file to load the language file from.
     * @return TaterzenLanguage object
     */
    public static TaterConfig loadConfigFile(File file) {
        TaterConfig config;
        if (file.exists()) {
            try (BufferedReader fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                config = gson.fromJson(fileReader, TaterConfig.class);
            } catch (IOException e) {
                throw new RuntimeException(MODID + " Problem occurred when trying to load config: ", e);
            }
        }
        else {
            config = new TaterConfig();
        }
        config.saveConfigFile(file);

        return config;
    }

    /**
     * Saves the config to the given file.
     *
     * @param file file to save config to
     */
    public void saveConfigFile(File file) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            getLogger().error("Problem occurred when saving config: " + e.getMessage());
        }
    }
}
