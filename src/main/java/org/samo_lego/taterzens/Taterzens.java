package org.samo_lego.taterzens;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.commands.TaterzensCommand;
import org.samo_lego.taterzens.npc.TaterzenNPC;

public class Taterzens implements ModInitializer {

    public static final String MODID = "taterzens";

    public static final EntityType<TaterzenNPC> TATERZEN = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(MODID, "npc"),
            FabricEntityTypeBuilder
                    .<TaterzenNPC>create(SpawnGroup.MONSTER, TaterzenNPC::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
                    .build()
    );

    @Override
    public void onInitialize() {
        // Hooray
        CommandRegistrationCallback.EVENT.register(TaterzensCommand::register);
        CommandRegistrationCallback.EVENT.register(NpcCommand::register);

        FabricDefaultAttributeRegistry.register(TATERZEN, TaterzenNPC.createMobAttributes());
    }
}
