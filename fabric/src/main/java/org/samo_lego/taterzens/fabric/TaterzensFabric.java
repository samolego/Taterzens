package org.samo_lego.taterzens.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.api.TaterzensAPI;
import org.samo_lego.taterzens.commands.edit.messages.MessagesReorderCommand;
import org.samo_lego.taterzens.fabric.compatibility.carpet.AdditionalFunctions;
import org.samo_lego.taterzens.fabric.compatibility.carpet.ScarpetProfession;
import org.samo_lego.taterzens.fabric.compatibility.carpet.ScarpetTraitCommand;
import org.samo_lego.taterzens.fabric.event.BlockInteractEventImpl;
import org.samo_lego.taterzens.fabric.platform.FabricPlatform;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import static org.samo_lego.taterzens.Taterzens.TATERZEN_TYPE;
import static org.samo_lego.taterzens.compatibility.ModDiscovery.CARPETMOD_LOADED;

public class TaterzensFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // Common initialization
        new Taterzens(new FabricPlatform());

        FabricDefaultAttributeRegistry.register(TATERZEN_TYPE.get(), TaterzenNPC.createTaterzenAttributes());

        // CarpetMod
        if (CARPETMOD_LOADED) {
            TaterzensAPI.registerProfession(ScarpetProfession.ID, ScarpetProfession::new);
            AdditionalFunctions.init();
        }

        // Events
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            Taterzens.registerCommands(dispatcher);
            MessagesReorderCommand.register(dispatcher);
            ScarpetTraitCommand.register();
        });

        UseBlockCallback.EVENT.register(new BlockInteractEventImpl());
    }
}