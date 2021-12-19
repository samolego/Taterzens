package org.samo_lego.taterzens.api.professions;

import org.samo_lego.taterzens.npc.TaterzenNPC;

public abstract class AbstractProfession implements TaterzenProfession {

    protected TaterzenNPC npc;

    public AbstractProfession(TaterzenNPC npc) {
        this.npc = npc;
    }
}
