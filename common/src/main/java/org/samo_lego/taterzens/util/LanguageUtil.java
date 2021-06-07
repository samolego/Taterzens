package org.samo_lego.taterzens.util;

import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.storage.TaterConfig;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.samo_lego.taterzens.Taterzens.*;

public class LanguageUtil {
    public static void init() {
        URL url = Taterzens.class.getResource(String.format("/data/%s/lang/%s.json", MODID, config.language));
        try {
            File langFile = new File(url.toURI().getPath());
            //File langFile = new File(taterDir + "/" + config.language + ".json");
            System.out.println(langFile.exists() + " " + langFile.getPath());

            if(!langFile.exists()) {
                config.language = "en_us";
                config.saveConfigFile(new File(taterDir + "/config.json"));

                // Extract language from jar
                url = Taterzens.class.getResource(String.format("/data/%s/lang/en_us.json", MODID));
                langFile = new File(url.toURI().getPath());
            }
            lang = TaterConfig.loadLanguageFile(langFile);
        } catch(URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
