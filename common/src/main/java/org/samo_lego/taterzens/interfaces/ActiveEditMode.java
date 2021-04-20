package org.samo_lego.taterzens.interfaces;

/**
 * Getting / srtting the editor mode for player's Taterzen.
 */
public interface ActiveEditMode {

    /**
     * Sets the active editor mode for the selected Taterzen.
     * @param mode editor mode type
     */
    void setEditorMode(Types mode);

    /**
     * Gets current edit mode type.
     * @return current editor mode; NONE is default
     */
    Types getEditorMode();

    enum Types {
        NONE,
        MESSAGES,
        PATH,
        EQUIPMENT
    }
}
