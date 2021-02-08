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
            .create();


    public String language = "en_us";


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
        config.saveLanguageFile(file);

        return config;
    }

    /**
     * Saves the language to the given file.
     *
     * @param file file to save config to
     */
    public void saveLanguageFile(File file) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            getLogger().error("Problem occurred when saving config: " + e.getMessage());
        }
    }
}
