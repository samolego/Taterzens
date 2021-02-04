package org.samo_lego.taterzens;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.commands.TaterzensCommand;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.HashSet;

public class Taterzens implements ModInitializer {

    public static final HashSet<TaterzenNPC> TATERZENS = new HashSet<>();

    @Override
    public void onInitialize() {
        // Hooray
        CommandRegistrationCallback.EVENT.register(TaterzensCommand::register);
        CommandRegistrationCallback.EVENT.register(NpcCommand::register);
    }
}
