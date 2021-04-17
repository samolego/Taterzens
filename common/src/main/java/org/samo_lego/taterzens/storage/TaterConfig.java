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

    public final String _comment_language = "// Language file to use.";
    /**
     * Language file used by Taterzens.
     *
     * Located at $minecraftFolder/config/Taterzens/$lang.json
     */
    public String language = "en_us";

    public final String _comment_disableRegistrySync1 = "// Whether to remove Taterzens from Fabric's registry sync. Safe to be true.";
    public final String _comment_disableRegistrySync2 = "// If using Forge, this will remove WHOLE registry sync. Proceed with caution.";
    /**
     * Whether to disable Fabric's / Forge's registry sync.
     * This marks mod as serverside.
     *
     * If using Fabric, Taterzen NPC type is just removed from sync packet.
     * If using Forge, the whole thing is not synced. Use carefully!
     */
    @SerializedName("disable_registry_sync")
    public boolean disableRegistrySync = true;

    public final String _comment_taterzenTablistTimeout1 = "// After how many ticks Taterzens should be cleared from tablist.";
    public final String _comment_taterzenTablistTimeout2 = "// Some delay is needed, otherwise clients don't fetch their skins.";
    /**
     * After how many ticks Taterzens should be cleared from tablist.
     * Some delay is needed, otherwise clients don't fetch their skins.
     */
    @SerializedName("taterzen_tablist_timeout")
    public int taterzenTablistTimeout = 30;


    public final String _comment_fabricTailorAdvert = "// Whether to remind you that if FabricTailor mod is installed, it has some more skin functionality for Taterzens as well.";
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
    public Permissions perms = new Permissions();

    /**
     * Some permission stuff.
     * If you are looking for permission nodes,
     * see the generated permission.json file.
     *
     * (You must have LuckPerms installed.)
     */
    public static class Permissions {
        public final String _comment_npcCommandPermissionLevel1 = "// Permission level required to execute /npc command.";
        public final String _comment_npcCommandPermissionLevel2 = "// Valid only if LuckPerms isn't present.";
        /**
         * Permission level required to execute /npc command.
         * Valid only if LuckPerms isn't present.
         */
        @SerializedName("npc_command_permission_level")
        public int npcCommandPermissionLevel = 2;


        public final String _comment_taterzensCommandPermissionLevel1 = "// Permission level required to execute /taterzens command.";
        public final String _comment_taterzensCommandPermissionLevel2 = "// Valid only if LuckPerms isn't present.";
        /**
         * Permission level required to execute /taterzens command.
         * Valid only if LuckPerms isn't present.
         */
        @SerializedName("taterzens_command_permission_level")
        public int taterzensCommandPermissionLevel = 4;

        public final String _comment_allowSettingHigherPermissionLevel1 = "// Whether to allow players to set the permission level";
        public final String _comment_allowSettingHigherPermissionLevel2 = "// of Taterzen higher than their own. Careful! This could";
        public final String _comment_allowSettingHigherPermissionLevel3 = "// enable players to bypass their permission level with NPC.";
        @SerializedName("allow_setting_higher_perm_level")
        public boolean allowSettingHigherPermissionLevel = false;
    }

    /**
     * Default {@link org.samo_lego.taterzens.npc.TaterzenNPC} settings.
     */
    public static class Defaults {
        public final String _comment_name = "// Default settings for new Taterzens.";
        /**
         * Default Taterzen name
         */
        public String name = "Taterzen";
        /**
         * Whether Taterzens should be leashable.
         */
        public boolean leashable = false;
        public boolean pushable = false;

        public final String _comment_commandPermissionLevel = "// Default command permission level of Taterzen.";
        /**
         * Default command permission level of Taterzen.
         */
        @SerializedName("command_permission_level")
        public int commandPermissionLevel = 4;

        public final String _comment_sounds = "// Default sounds for Taterzens. Set to null to mute them.";
        /**
         * Default Taterzen death sound.
         * Can be null to not produce any sounds.
         */
        @SerializedName("death_sound")
        public String deathSound = "entity.player.death";
        /**
         * Default Taterzen hurt / hit sound.
         * Can be null to not produce any sounds.
         */
        @SerializedName("hurt_sound")
        public String hurtSound = "entity.player.hurt";
        /**
         * Default Taterzen ambient sound.
         * Can be null to not produce any sounds.
         */
        @SerializedName("ambient_sound")
        public String ambientSound = "entity.player.breath";
        /**
         * Whether Taterzen is invulnerable by default.
         */
        public boolean invulnerable = true;
    }

    /**
     * Settings for Taterzen's messages
     */
    public static class Messages {
        public final String _comment_messageDelay = "// Default delay between each message, in ticks.";
        /**
         * Default delay between each message, in ticks.
         */
        @SerializedName("message_delay")
        public int messageDelay = 100;
        public final String _comment_exitEditorAfterMsgEdit = "// Whether to exit message editor mode after editing a message.";
        /**
         * Whether to exit message editor mode after editing a message.
         */
        @SerializedName("exit_editor_after_msg_edit")
        public boolean exitEditorAfterMsgEdit = true;
    }


    /**
     * Settings for path visualisation.
     */
    public static class Path {
        public Path.Color color = new Path.Color();
        /**
         * Color of particles used in path editor.
         * Accepts RGB values (0 - 255).
         */
        public static class Color {
            public final String _comment = "// Which color of particles to use in path editor. Use RGB values ( 0 - 255 ).";
            public float red = 0;
            public float green = 255 ;
            public float blue = 255;
        }
    }


    /**
     * Loads config file.
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
