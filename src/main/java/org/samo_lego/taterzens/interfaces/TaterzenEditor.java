package org.samo_lego.taterzens.interfaces;

import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.npc.TaterzenNPC;

/**
 * Interface for players who work with TaterzenNPCs.
 */
public interface TaterzenEditor {
    @Nullable
    TaterzenNPC getNpc();

    void selectNpc(@Nullable TaterzenNPC npc);

    /**
     * Whether player is in path edit mode.
     * If true, blocks that are broken will
     * be added to {@link TaterzenNPC NPC's} path.
     * @return true if player is in path edit mode, otherwise false.
     */
    boolean inPathEditMode();

    /**
     * Sets the player's path edit mode property
     * for selected {@link TaterzenNPC}.
     * @param editMode edit mode status
     */
    void setPathEditMode(boolean editMode);

    /**
     * Whether player is in message edit mode.
     * If true, messages sent to chat
     * will be redirected to {@link org.samo_lego.taterzens.npc.NPCData#messages}.
     * @return true if player is in message edit mode, otherwise false.
     */
    boolean inMsgEditMode();
    /**
     * Sets the player's message edit mode property
     * for selected {@link TaterzenNPC}.
     * @param editMode edit mode status
     */
    void setMsgEditMode(boolean editMode);

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
}
