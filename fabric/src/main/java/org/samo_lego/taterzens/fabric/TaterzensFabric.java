package org.samo_lego.taterzens.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.samo_lego.taterzens.Taterzens;
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
            AdditionalFunctions.init();
        }

        // Events
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            Taterzens.registerCommands(dispatcher, context);
            MessagesReorderCommand.register(dispatcher);
            if (CARPETMOD_LOADED) {
                ScarpetTraitCommand.register(); // also registers profession on first init
            }
        });

        UseBlockCallback.EVENT.register(new BlockInteractEventImpl());
    }
}