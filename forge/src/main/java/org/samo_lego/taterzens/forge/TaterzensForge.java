package org.samo_lego.taterzens.forge;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.forge.event.EventHandler;
import org.samo_lego.taterzens.forge.platform.ForgePlatform;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import static org.samo_lego.taterzens.Taterzens.MOD_ID;
import static org.samo_lego.taterzens.Taterzens.NPC_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(MOD_ID)
public class TaterzensForge {

    public TaterzensForge() {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        var tt = RegistryObject.create(NPC_ID, ForgeRegistries.ENTITIES);
        EntityType<TaterzenNPC> entityType = (EntityType<TaterzenNPC>) tt.get();

        new Taterzens(new ForgePlatform());
    }
}