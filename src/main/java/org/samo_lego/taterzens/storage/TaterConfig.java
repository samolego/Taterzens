package org.samo_lego.taterzens.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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


    /**
     * Language file used by Taterzens.
     *
     * Located at $minecraftFolder/config/Taterzens/$lang.json
     */
    public String language = "en_us";
    public Defaults defaults = new Defaults();
    public Path path = new Path();


    /**
     * Default {@link org.samo_lego.taterzens.npc.TaterzenNPC} settings.
     */
    public static class Defaults {
        public String _comment = "// Default settings for new Taterzens.";
        public String name = "Taterzen";
        public boolean leashable = false;
        public boolean pushable = false;
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
