package org.samo_lego.taterzens.api.professions;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.nbt.CompoundTag;
import org.samo_lego.taterzens.npc.TaterzenNPC;

public interface ProfessionParseCallback {
    Event<ProfessionParseCallback> EVENT = EventFactory.createArrayBacked(ProfessionParseCallback.class,
        (listeners) -> (tag, taterzen) -> {
            for (ProfessionParseCallback listener : listeners) {
                listener.parseProfession(tag, taterzen);
            }
    });

    void parseProfession(CompoundTag tag, TaterzenNPC taterzen);
}
