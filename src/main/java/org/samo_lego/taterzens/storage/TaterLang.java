package org.samo_lego.taterzens.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.samo_lego.taterzens.Taterzens.MODID;
import static org.samo_lego.taterzens.Taterzens.getLogger;

public class TaterLang {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();


    public String availableTaterzens = "Available Taterzens:";
    public String showLoadedTaterzens = "Show loaded Taterzens";
    public String fabricTailorAdvert = "If you want more skin options for Taterzens, install FabricTailor mod.";
    public String skinCommandUsage = "To customize the skin even more, use FabricTailor's built in /skin command.";
    public String taterzenMessages = "Taterzen %s has the following messages. Click on one to edit it.";
    public String editMessageMode = "You are now editing message: %s. Click enter after editing to save it.";
    public String enterMessageEditor = "Click to enter message editing mode.";
    public String taterzenCommands = "Taterzen %s will execute the following commands on right-click.";

    public static class Success {
        public String spawnedTaterzen = "Taterzen %s has been spawned successfully.";
        public String changedEntityType = "Taterzen type was set to %s.";
        public String resetEntityType = "Entity type for %s was reset to default.";
        public String killedTaterzen = "Taterzen %s has been removed.";
        public String setCommandAction = "Command is now set to %s.";
        public String selectedTaterzen = "You have selected %s.";
        public String equipmentEditorEnter = "You've entered equipment editor for %s. Enter same command to exit.";
        public String equipmentEditorDescLine1 = "Right click the Taterzen to equip it.";
        public String equipmentEditorDescLine2 = "To put armor in hand, use shift right click.";
        public String equipmentEditorDescLine3 = "Punch to swap hands.";
        public String equipmentEditorDescLine4 = "Shift right click with empty hand to drop all equipment.";
        public String editorExit = "You've exited the editor.";
        public String taterzenSkinChange = "Skin of Taterzen was fetched from %s.";
        public String configReloaded = "Config was reloaded successfully.";
        public String changedMovementType = "Movement type was set to %s.";
        public String renameTaterzen = "Taterzen has been renamed to %s.";
        public String exportedTaterzen = "Taterzen has been exported to %s.";
        public String importedTaterzenPreset = "Taterzen was successfully loaded from %s.";
        public String pathEditorEnter = "You've entered path editor for %s. Enter same command to exit.";
        public String pathEditorDescLine1 = "Left click the blocks to add them to the path.";
        public String pathEditorDescLine2 = "Right click the blocks to remove them to the path.";
        public String clearPath = "Path for %s was cleared successfully.";
        public String msgEditorEnter = "You've entered message editor for %s. Enter same command to exit.";
        public String msgEditorDescLine1 = "Send messages in chat and %s will repeat them.";
        public String msgEditorDescLine2 = "You can use normal text or tellraw structure (for colors).";
        public String messageAdded = "Message %s was added successfully.";
        public String messagesCleared = "Taterzen %s has been muted.";
        public String messageDelaySet = "Message delay for the selected message is now %s.";
        public String messageChanged = "Message has been changed to %s.";
        public String messageDeleted = "Message %s has been deleted successfully.";
        public String deselectedTaterzen = "Your Taterzen selection has been cleared.";
        public String updatedPermissionLevel = "Permission level for command execution is now set to %s.";
        public String commandsCleared = "Commands for %s have been cleared.";
        public String commandRemoved = "Command %s has been removed successfully.";
        public String skinLayersMirrored = "Skin layers for %s were mirrored from you successfully.";
        public String behaviour = "Behaviour is now set to %s.";
        public String behaviourSuggestion = "You'd probably want to set invulnerability to false as well.";
        public String invulnerableStatus = "Invulnerable status has been set to %s.";
        public String equipmentDropStatus = "Equipment drops have been set to %s.";
        public String professionChanged = "Profession was set to %s.";
    }

    public static class Error {
        public String selectTaterzen = "You have to select Taterzen first.";
        public String noTaterzenFound = "No Taterzens with id %s were found.";
        public String noPresetFound = "No Taterzen preset with name %s was found.";
        public String cannotReadPreset = "Preset %s cannot be read.";
        public String invalidEntityId = "Entity id %s is invalid.";
        public String invalidText = "The text nbt you entered is invalid.";
        public String noMessageFound = "No messages with id %s were found.";
        public String enterMessageEditorMode = "You need to enter message editor mode to use this feature.";
        public String noCommandFound = "No commands with id %s were found.";
        public String disguiseLibRequired = "DisguiseLib mod is required for this action.";
    }


    public TaterLang.Success success = new TaterLang.Success();
    public TaterLang.Error error = new TaterLang.Error();


    /**
     * Loads language file.
     *
     * @param file file to load the language file from.
     * @return TaterzenLanguage object
     */
    public static TaterLang loadLanguageFile(File file) {
        TaterLang language;
        if(file.exists()) {
            try (BufferedReader fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                language = gson.fromJson(fileReader, TaterLang.class);
            } catch (IOException e) {
                throw new RuntimeException(MODID + " Problem occurred when trying to load language: ", e);
            }
        }
        else {
            language = new TaterLang();
        }
        language.saveLanguageFile(file);

        return language;
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
            getLogger().error("Problem occurred when saving language file: " + e.getMessage());
        }
    }
}
