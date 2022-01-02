package org.samo_lego.taterzens.interfaces;

import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.npc.TaterzenNPC;

/**
 * Interface for players who edit TaterzenNPCs.
 */
public interface ITaterzenEditor {
    /**
     * Gets the selected Taterzen of player.
     * @return selected Taterzen
     */
    @Nullable
    TaterzenNPC getNpc();

    /**
     * Selects {@link TaterzenNPC} to be editoed.
     * @param npc Taterzen to select
     * @return true if successful, false if taterzen is locked and cannot be selected by this player.
     */
    boolean selectNpc(@Nullable TaterzenNPC npc);

    /**
     * Sets the index of message that's
     * being edited by the player.
     * Range: 0 - (size of {@link org.samo_lego.taterzens.npc.NPCData#messages} array - 1)
     * @param selected selected message in the messages array
     */
    void setEditingMessageIndex(int selected);

    /**
     * Gets the index of the message
     * player is editing for selected
     * {@link TaterzenNPC}.
     * @return index of message being edited in {@link org.samo_lego.taterzens.npc.NPCData#messages}
     */
    int getEditingMessageIndex();

    /**
     * Sets the active editor mode for the selected Taterzen.
     * @param mode editor mode type
     */
    void setEditorMode(EditorMode mode);

    /**
     * Gets current edit mode type.
     * @return current editor mode; NONE is default
     */
    EditorMode getEditorMode();

    /**
     * Available editor modes.
     */
    enum EditorMode {
        NONE,
        MESSAGES,
        PATH,
        EQUIPMENT
    }
}
