package org.samo_lego.taterzens.interfaces;

import org.samo_lego.taterzens.npc.TaterzenNPC;

/**
 * Interface for players who work with TaterzenNPCs.
 */
public interface TaterzenEditor {
    TaterzenNPC getNpc();
    void selectNpc(TaterzenNPC npc);

    boolean inPathEditMode();
    void setPathEditMode(boolean editMode);

    boolean inMsgEditMode();
    void setMsgEditMode(boolean editMode);
}
