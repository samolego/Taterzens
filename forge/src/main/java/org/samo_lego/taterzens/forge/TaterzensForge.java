package org.samo_lego.taterzens.forge;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
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
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);
    public static final RegistryObject<EntityType<TaterzenNPC>> TATERZEN = ENTITIES.register(NPC_ID.getPath(), () -> EntityType.Builder
            .of(TaterzenNPC::new, MobCategory.MISC)
            .sized(0.6F, 1.8F)
            .build(NPC_ID.getPath()));

    public TaterzensForge() {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());

        new Taterzens(new ForgePlatform());
    }
}