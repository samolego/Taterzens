package org.samo_lego.taterzens.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.samo_lego.taterzens.Taterzens;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.samo_lego.taterzens.Taterzens.*;

public class LanguageUtil {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    private static final InputStream DEFAULT_LANG_STREAM = Taterzens.class.getResourceAsStream("/data/taterzens/lang/en_us.json");

    /**
     * Initializes the mod's language json object.
     */
    public static void setupLanguage() {
        String langPath = String.format("/data/taterzens/lang/%s.json", config.language);
        InputStream stream = Taterzens.class.getResourceAsStream(langPath);
        if(stream == null) {
            //todo check GH for latest translations

            System.out.println("ohno, invalid lang file");
            // Since this language doesn't exist,
            // change the config back to english.
            config.language = "en_us";
            config.saveConfigFile(new File(taterDir + "/config.json"));

            lang = loadLanguageFile(DEFAULT_LANG_STREAM);
        } else {
            lang = loadLanguageFile(stream);
        }
    }

    /**
     * Loads language file.
     *
     * @param inputStream lang file input stream.
     * @return JsonObject containing language keys and values.
     */
    public static JsonObject loadLanguageFile(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException("[Taterzens]: Problem occurred when trying to load language: ", e);
        }
    }
}
