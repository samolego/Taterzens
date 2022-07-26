package org.samo_lego.taterzens.forge;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.forge.event.EventHandler;
import org.samo_lego.taterzens.forge.platform.ForgePlatform;

import static org.samo_lego.taterzens.Taterzens.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(MOD_ID)
public class TaterzensForge {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);

    public TaterzensForge() {
        var evtBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        ENTITIES.register(evtBus);

        new Taterzens(new ForgePlatform());
    }
}