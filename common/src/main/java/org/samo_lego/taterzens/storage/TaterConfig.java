package org.samo_lego.taterzens.storage;

import com.google.gson.annotations.SerializedName;
import org.samo_lego.config2brigadier.IBrigadierConfigurator;
import org.samo_lego.config2brigadier.annotation.BrigadierDescription;
import org.samo_lego.config2brigadier.annotation.BrigadierExcluded;
import org.samo_lego.taterzens.Taterzens;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.samo_lego.taterzens.Taterzens.GSON;
import static org.samo_lego.taterzens.Taterzens.LOGGER;
import static org.samo_lego.taterzens.Taterzens.MOD_ID;

public class TaterConfig implements IBrigadierConfigurator {

    @SerializedName("// Language file to use.")
    public final String _comment_language = "";
    /**
     * Language file used by Taterzens.
     *
     * Located at $minecraftFolder/config/Taterzens/$lang.json
     */
    @BrigadierExcluded
    public String language = "en_us";

    @SerializedName("// After how many ticks Taterzens should be cleared from tablist.")
    public final String _comment_taterzenTablistTimeout0 = "";
    @SerializedName("// Some delay is needed, otherwise clients don't fetch their skins.")
    public final String _comment_taterzenTablistTimeout1 = "";
    @SerializedName("// If you want them to stay on the tablist, set this to -1.")
    public final String _comment_taterzenTablistTimeout2 = "";
    /**
     * After how many ticks Taterzens should be cleared from tablist.
     * Some delay is needed, otherwise clients don't fetch their skins.
     */
    @BrigadierDescription(defaultOption = "30")
    @SerializedName("taterzen_tablist_timeout")
    public int taterzenTablistTimeout = 30;


    @SerializedName("// Whether to remind you that if FabricTailor mod is installed,")
    public final String _comment_fabricTailorAdvert0 = "";
    @SerializedName("// it has some built-in skin swapping functionality for Taterzens as well.")
    public final String _comment_fabricTailorAdvert1 = "";
    /**
     * Whether to remind you that if FabricTailor
     * mod is installed, it has some more skin functionality.
     *
     * @see <a href="https://github.com/samolego/FabricTailor">FabricTailor</a>
     */
    @SerializedName("post_fabrictailor_advert")
    public boolean fabricTailorAdvert = false;

    @SerializedName("// Whether to cancel sending info that Taterzen has executed a command to ops.")
    public final String _comment_hideOpsMessage = "";
    @SerializedName("hide_ops_message")
    public boolean hideOpsMessage = true;

    @SerializedName("// Whether to automatically lock the taterzen after creating it.")
    public final String _comment_lockAfterCreation = "";
    @BrigadierDescription(defaultOption = "true")
    @SerializedName("lock_after_creation")
    public boolean lockAfterCreation = true;

    @SerializedName("// Obsucre Taterzens' names while they are visible on the tab list.")
    public boolean obscureTabList = true;

    @SerializedName("// Default settings for new Taterzens.")
    public final String _comment_defaults = "";
    public Defaults defaults = new Defaults();

    public Path path = new Path();
    public Messages messages = new Messages();
    public Permissions perms = new Permissions();
    public Bungee bungee = new Bungee();

    @SerializedName("// Custom model data number that items in GUI should use.")
    public final String _comment_guiItemModelData = "";
    @BrigadierDescription(defaultOption = "257")
    @SerializedName("gui_item_model_data")
    public int guiItemModelData = 257;

    @SerializedName("// Nodes which prefer execution instead of going in submenus in `/npc` gui. (Swaps the right and left click function to these nodes)")
    public final String _comment_prefersExecution = "";
    @SerializedName("prefer_execution_nodes")
    public List<String> prefersExecution = List.of(
            "npc edit messages swap",
            "npc edit equipment"
    );

    @SerializedName("// Whether to allow Taterzens to fight players in peaceful mode as well.")
    public final String _comment_combatInPeaceful = "";
    @BrigadierDescription(defaultOption = "true")
    @SerializedName("combat_in_peaceful")
    public boolean combatInPeaceful = true;


    @SerializedName("// Whether give fake glowing effect to Taterzen when selected.")
    public final String _comment_glowSelectedNpc = "";
    @BrigadierDescription(defaultOption = "true")
    @SerializedName("glow_selected_npc")
    public boolean glowSelectedNpc = true;

    @Override
    public void save() {
        this.saveConfigFile(Taterzens.getInstance().getConfigFile());
    }

    /**
     * Some permission stuff.
     * If you are looking for permission nodes,
     * see the generated permission.toml file.
     *
     * (You must have LuckPerms installed for it to generate.)
     */
    public static class Permissions {
        @SerializedName("// Whether to save all permissions into permissions.toml file if LuckPerms is loaded.")
        public final String _comment_savePermsFile = "";
        @SerializedName("save_permissions_file")
        public boolean savePermsFile = true;

        @SerializedName("// Permission level required to execute /npc command.")
        public final String _comment_npcCommandPermissionLevel0 = "";
        @SerializedName("// Valid only if LuckPerms isn't present.")
        public final String _comment_npcCommandPermissionLevel1 = "";
        /**
         * Permission level required to execute /npc command.
         * Valid only if LuckPerms isn't present.
         */
        @SerializedName("npc_command_permission_level")
        public int npcCommandPermissionLevel = 2;

        @SerializedName("// Default permission level for `/profession` command.")
        public final String _comment_professionCommandPermissionLevel = "";
        @SerializedName("profession_command_permission_level")
        public int professionCommandPL = 2;


        @SerializedName("// Permission level required to execute / taterzens command.")
        public final String _comment_taterzensCommandPermissionLevel0 = "";
        @SerializedName("// Again, valid only if LuckPerms isn't present.")
        public final String _comment_taterzensCommandPermissionLevel1 = "";
        /**
         * Permission level required to execute /taterzens command.
         * Valid only if LuckPerms isn't present.
         */
        @SerializedName("taterzens_command_permission_level")
        public int taterzensCommandPermissionLevel = 4;

        @SerializedName("// Whether to allow players to set the permission level")
        public final String _comment_allowSettingHigherPermissionLevel0 = "";
        @SerializedName("// of Taterzen higher than their own. Careful! This could")
        public final String _comment_allowSettingHigherPermissionLevel1 = "";
        @SerializedName("// enable players to bypass their permission level with NPC.")
        public final String _comment_allowSettingHigherPermissionLevel2 = "";
        @SerializedName("allow_setting_higher_perm_level")
        public boolean allowSettingHigherPermissionLevel = false;

        @SerializedName("// Default permission level to bypass selecting locked npcs.")
        public final String _comment_selectBypassLevel = "";
        @BrigadierDescription(defaultOption = "3")
        @SerializedName("select_bypass_level")
        public int selectBypassLevel = 3;
    }

    /**
     * Default {@link org.samo_lego.taterzens.npc.TaterzenNPC} settings.
     */
    public static class Defaults {
        /**
         * Default Taterzen name
         */
        public String name = "Taterzen";
        /**
         * Whether Taterzens should be leashable.
         */
        public boolean leashable = false;
        public boolean pushable = false;

        @SerializedName("// How many ticks must pass between each interaction (command triggerings).")
        public final String _comment_minInteractionTime = "";
        @BrigadierDescription(defaultOption = "0")
        @SerializedName("minimum_interaction_time")
        public long minInteractionTime = 0;

        @SerializedName("// Default cooldown message to be sent to the player. Supports translation as well.")
        public final String _comment_commandCooldownMessage = "";
        @SerializedName("command_cooldown_message")
        public String commandCooldownMessage = "taterzens.npc.interact.cooldown";

        @SerializedName("// Default command permission level of Taterzen.")
        public final String _comment_commandPermissionLevel = "";
        /**
         * Default command permission level of Taterzen.
         */
        @SerializedName("command_permission_level")
        public int commandPermissionLevel = 4;

        @SerializedName("// Default sounds for Taterzens. Set to [] to mute them.")
        public final String _comment_sounds = "";
        /**
         * Default Taterzen death sound.
         * Can be null to not produce any sounds.
         */
        @SerializedName("death_sounds")
        public List<String> deathSounds = new ArrayList<>(List.of(
                "entity.player.death"
        ));
        /**
         * Default Taterzen hurt / hit sound.
         * Can be null to not produce any sounds.
         */
        @SerializedName("hurt_sounds")
        public List<String> hurtSounds = new ArrayList<>(List.of(
                "entity.player.hurt"
        ));
        /**
         * Default Taterzen ambient sound.
         * Can be null to not produce any sounds.
         */
        @SerializedName("ambient_sounds")
        public List<String> ambientSounds = new ArrayList<>();

        @SerializedName("// Whether Taterzen is invulnerable by default.")
        public final String _comment_invulnerable = "";
        /**
         * Whether Taterzen is invulnerable by default.
         */
        @BrigadierDescription(defaultOption = "true")
        public boolean invulnerable = true;

        @BrigadierDescription(defaultOption = "true")
        @SerializedName("// Enable jumps when Taterzen is in attack mode.")
        public final String _comment_jumpWhileAttacking = "";
        /**
         * Whether to enable jumps when Taterzen
         * is in attack mode.
         */
        @BrigadierDescription(defaultOption = "true")
        @SerializedName("jump_while_attacking")
        public boolean jumpWhileAttacking = true;

        @SerializedName("// Whether Taterzen is able to fly.")
        public final String _comment_allowFlight = "";

        @BrigadierDescription(defaultOption = "false")
        @SerializedName("allow_flight")
        public boolean allowFlight = false;

        @SerializedName("// Whether Taterzens can be picked up by boats / minecarts.")
        public final String _comment_allowRiding = "";
        @BrigadierDescription(defaultOption = "false")
        @SerializedName("allow_riding")
        public boolean allowRiding = false;

        @SerializedName("// Whether Taterzen is able to swim.")
        public final String _comment_allowSwim = "";
        @BrigadierDescription(defaultOption = "true")
        @SerializedName("allow_swim")
        public boolean allowSwim = true;

        @SerializedName("// Whether Taterzens should drop equipment on death.")
        public final String _comment_dropEquipment = "";
        @SerializedName("drop_equipment")
        @BrigadierDescription(defaultOption = "false")
        public boolean dropEquipment = false;
    }

    /**
     * Settings for Taterzen's messages
     */
    public static class Messages {
        @SerializedName("// Default delay between each message, in ticks.")
        public final String _comment_messageDelay = "";
        /**
         * Default delay between each message, in ticks.
         */
        @SerializedName("message_delay")
        public int messageDelay = 100;
        @SerializedName("// Whether to exit message editor mode after editing a message.")
        public final String _comment_exitEditorAfterMsgEdit = "";
        /**
         * Whether to exit message editor mode after editing a message.
         */
        @SerializedName("exit_editor_after_msg_edit")
        public boolean exitEditorAfterMsgEdit = true;

        @SerializedName("// Message format. First %s is replaced with name, second one with message.")
        public final String _comment_structure = "";
        public String structure = "%s -> you: %s";

        @SerializedName("// How far can player be for messages start appearing.")
        public final String _comment_speakDistance = "(default: 3.0f)";
        @SerializedName("speak_distance")
        public float speakDistance = 3.0f;
    }

    /**
     * Settings for path visualisation.
     */
    public static class Path {
        @SerializedName("// Which color of particles to use in path editor. Use RGB values ( 0 - 255 ).")
        public final String _comment_color = "";
        public Color color = new Color();
        /**
         * Color of particles used in path editor.
         * Accepts RGB values (0 - 255).
         */
        public static class Color {
            public float red = 0;
            public float green = 255;
            public float blue = 255;
        }
    }


    /**
     * Settings for proxy connections.
     */
    public static class Bungee {

        @SerializedName("// Whether to enable bungee commands feature for NPCs.")
        public final String _comment_enableCommands = "";
        @SerializedName("enable_commands")
        public boolean enableCommands = false;
    }


    /**
     * Loads config file.
     *
     * @param file file to load the language file from.
     * @return TaterzenLanguage object
     */
    public static TaterConfig loadConfigFile(File file) {
        TaterConfig config = null;
        if (file.exists()) {
            try (BufferedReader fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                config = GSON.fromJson(fileReader, TaterConfig.class);
            } catch (IOException e) {
                throw new RuntimeException(MOD_ID + " Problem occurred when trying to load config: ", e);
            }
        }
        if(config == null)
            config = new TaterConfig();

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
            GSON.toJson(this, writer);
        } catch (IOException e) {
            LOGGER.error("Problem occurred when saving config: " + e.getMessage());
        }
    }

    /**
     * Changes values of current object with reflection,
     * in order to keep the same object.
     * (that still allows in-game editing)
     *
     */
    public void reload() {
        TaterConfig newConfig = loadConfigFile(Taterzens.getInstance().getConfigFile());
        this.reload(newConfig);
    }
}
