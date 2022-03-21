package org.samo_lego.taterzens.forge;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.forge.event.EventHandler;
import org.samo_lego.taterzens.forge.platform.ForgePlatform;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import static org.samo_lego.taterzens.Taterzens.MOD_ID;
import static org.samo_lego.taterzens.Taterzens.NPC_ID;
import static org.samo_lego.taterzens.Taterzens.TATERZEN_TYPE;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(MOD_ID)
public class TaterzensForge {

    public TaterzensForge() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.addListener(TaterzensForge::entityAttributes);

        new Taterzens(new ForgePlatform());
    }

    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(TATERZEN_TYPE, TaterzenNPC.createTaterzenAttributes().build());
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        TATERZEN_TYPE = (EntityType<TaterzenNPC>) EntityType.Builder
                .of(TaterzenNPC::new, MobCategory.MISC)
                .sized(0.6F, 1.8F)
                .build(NPC_ID.toString())
                .setRegistryName(NPC_ID.toString());
        event.getRegistry().registerAll(TATERZEN_TYPE);
    }
}