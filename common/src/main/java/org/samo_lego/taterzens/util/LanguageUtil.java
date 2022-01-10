package org.samo_lego.taterzens.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.samo_lego.taterzens.Taterzens;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;
import static org.samo_lego.taterzens.Taterzens.GSON;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.Taterzens.lang;

public class LanguageUtil {

    public static final InputStream DEFAULT_LANG_STREAM = Taterzens.class.getResourceAsStream("/data/taterzens/lang/en_us.json");
    public static final List<String> LANG_LIST = new ArrayList<>();
    private static final String API_URL = "https://api.github.com/repos/samolego/taterzens/contents/common/src/main/resources/data/taterzens/lang";
    private static final String LANG_FILE_URL = "https://raw.githubusercontent.com/samolego/Taterzens/master/common/src/main/resources/data/taterzens/lang/%s.json";

    /**
     * Initializes the mod's language json object.
     */
    public static void setupLanguage() {
        String langPath = String.format("/data/taterzens/lang/%s.json", config.language);
        InputStream stream = Taterzens.class.getResourceAsStream(langPath);
        if(stream == null) {
            // Try to fetch language, as it's not present in jar
            try {
                final URL url = new URL(String.format(LANG_FILE_URL, config.language));
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

                int responseCode = conn.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK) {
                    LanguageUtil.loadLanguageFile(conn.getInputStream());
                } else {
                    getLogger("Taterzens").error("Got {} when trying to fetch {} from {}.", responseCode, config.language, url);

                    // Since this language doesn't exist,
                    // change the config back to english.
                    config.language = "en_us";
                    config.save();

                    lang = loadLanguageFile(DEFAULT_LANG_STREAM);
                }
                conn.disconnect();

            } catch(IOException e) {
                e.printStackTrace();
            }

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
            return GSON.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            getLogger("Taterzens").error("[Taterzens]: Problem occurred when trying to load language: ", e);
        }
        return new JsonObject();
    }


    static {
        try {
            final URL REPO_API_URL = new URL(API_URL);
            String ending = ".json";
            HttpsURLConnection conn = (HttpsURLConnection) REPO_API_URL.openConnection();
            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String reply = IOUtils.toString(new InputStreamReader(conn.getInputStream()));
                JsonArray json = GSON.fromJson(reply, JsonArray.class);

                for(JsonElement element : json) {
                    JsonObject file = element.getAsJsonObject();

                    String langName = file.get("name").getAsString();
                    if(langName.endsWith(ending))
                        LANG_LIST.add(langName.substring(0, langName.length() - ending.length()));
                }
            }
            conn.disconnect();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
