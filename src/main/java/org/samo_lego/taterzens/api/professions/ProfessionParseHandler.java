package org.samo_lego.taterzens.api.professions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import static org.samo_lego.taterzens.Taterzens.MODID;
import static org.samo_lego.taterzens.api.professions.DefaultProfession.TYPE;

public class ProfessionParseHandler implements ProfessionParseCallback {

    public ProfessionParseHandler() {}

    @Override
    public void parseProfession(CompoundTag tag, TaterzenNPC taterzen) {
        if(tag.getString("ProfessionType").equals(TYPE))
            taterzen.setProfession(new Identifier(MODID, TYPE), new DefaultProfession(taterzen));
    }
}
