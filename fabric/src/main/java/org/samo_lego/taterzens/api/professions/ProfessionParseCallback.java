package org.samo_lego.taterzens.api.professions;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.samo_lego.taterzens.npc.TaterzenNPC;

/**
 * A profession parsing callback interface
 * which you'd implement in your event handler.
 */
public interface ProfessionParseCallback {
    Event<ProfessionParseCallback> EVENT = EventFactory.createArrayBacked(ProfessionParseCallback.class,
        (listeners) -> (professionId, taterzen) -> {
            for (ProfessionParseCallback listener : listeners) {
                listener.parseProfession(professionId, taterzen);
            }
    });

    /**
     * Used to parse the profession from id
     * to a new profession object.
     * @param professionId string identifier of the profession
     * @param taterzen taterzen that should have this profession set.
     */
    void parseProfession(String professionId, TaterzenNPC taterzen);
}
