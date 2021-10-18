package org.samo_lego.taterzens.util;

import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Using slightly modified method from <a href="https://github.com/samolego/FabricTailor/blob/master/src/main/java/org/samo_lego/fabrictailor/util/SkinFetcher.java">FabricTailor</a> mod.
 */
public class WebUtil {

    /**
     * Gets content of a webpage as string. Used for API calls.
     *
     * @param url url to connect to.
     * @return reply string
     * @throws IOException if exception occurred.
     */
    @Nullable
    public static String urlRequest(URL url) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        String reply = null;

        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");

        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (
                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    Scanner scanner = new Scanner(isr)
            ) {
                StringBuilder replyBuilder = new StringBuilder();
                while(scanner.hasNextLine()) {
                    String line = scanner.next();
                    if(line.trim().isEmpty())
                        continue;
                    replyBuilder.append(line);
                }

                reply = replyBuilder.toString();
            }
        }
        connection.disconnect();

        return reply;
    }
}
