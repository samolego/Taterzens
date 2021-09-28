package org.samo_lego.taterzens;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.samo_lego.taterzens.forge.event.EventHandler;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.io.File;

import static org.samo_lego.taterzens.Taterzens.*;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(MODID)
public class TaterzensForge {

    public TaterzensForge() {
        taterDir = new File(FMLPaths.CONFIGDIR.get() + "/Taterzens/presets");
        DISGUISELIB_LOADED = ModList.get().isLoaded("disguiselib");

        //noinspection
        TATERZEN_TYPE = (EntityType<TaterzenNPC>) EntityType.Builder
                .of(TaterzenNPC::new, MobCategory.MISC)
                .sized(0.6F, 1.8F)
                .build(NPC_ID.toString())
                .setRegistryName(NPC_ID.toString());

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.addListener(TaterzensForge::entityAttributes);
        Taterzens.onInitialize();
    }

    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(TATERZEN_TYPE, TaterzenNPC.createTaterzenAttributes().build());
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().registerAll(TATERZEN_TYPE);
    }
}