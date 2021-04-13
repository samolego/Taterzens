package org.samo_lego.taterzens.api.professions;

import net.minecraft.util.Identifier;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import static org.samo_lego.taterzens.Taterzens.MODID;
import static org.samo_lego.taterzens.api.professions.DefaultProfession.TYPE;

public class ProfessionParseHandler implements ProfessionParseCallback {

    /**
     * A handler for the default profession type.
     */
    public ProfessionParseHandler() {
    }


    @Override
    public void parseProfession(String professionId, TaterzenNPC taterzen) {
        Identifier defaultProfessionId = new Identifier(MODID, TYPE);
        if(professionId.equals(defaultProfessionId.toString()))
            taterzen.setProfession(defaultProfessionId, new DefaultProfession(taterzen));
    }
}
