package org.samo_lego.taterzens.api.professions;

import org.samo_lego.taterzens.npc.TaterzenNPC;

/**
 * Class that you can extend to create your own professions.
 */
public class AbstractProfession implements TaterzenProfession {

    /**
     * TaterzenNPC that this profession is assigned to.
     */
    protected TaterzenNPC npc;

    public AbstractProfession() {
    }

    @Override
    public TaterzenProfession create(TaterzenNPC taterzen) {
        AbstractProfession profession = new AbstractProfession();
        profession.npc = taterzen;
        return profession;
    }
}
