package org.samo_lego.taterzens.util;

import org.apache.commons.io.FileUtils;
import org.samo_lego.taterzens.Taterzens;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class PermissionExtractor {

    /**
     * Extracts permissions file containing all permissions to given location.
     * @param destination file to save permissions to
     */
    public static void extractPermissionsFile(File destination) {
        final String perms = "/data/taterzens/perms/permissions.toml";
        InputStream stream = Taterzens.class.getResourceAsStream(perms);

        try {
            assert stream != null;
            FileUtils.copyInputStreamToFile(stream, destination);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
