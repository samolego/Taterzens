package org.samo_lego.taterzens.forge.platform;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryManager;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.samo_lego.taterzens.platform.Platform;

import java.nio.file.Path;
import java.util.Collections;

import static org.samo_lego.taterzens.Taterzens.MOD_ID;
import static org.samo_lego.taterzens.Taterzens.NPC_ID;
import static org.samo_lego.taterzens.Taterzens.TATERZEN_TYPE;
import static org.samo_lego.taterzens.commands.NpcCommand.npcNode;
import static org.samo_lego.taterzens.gui.EditorGUI.createCommandGui;

public class ForgePlatform extends Platform {

    private static final ResourceLocation ITEM_ID = new ResourceLocation("item");

    @Override
    public Path getConfigDirPath() {
        return FMLPaths.CONFIGDIR.get();

    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public int getItemRegistrySize() {
        return RegistryManager.ACTIVE.getRegistry(ITEM_ID).getValues().size();
    }

    @Override
    public boolean checkPermission(CommandSourceStack source, String permissionNode, int fallbackLevel) {
        return source.hasPermission(fallbackLevel);
    }

    @Override
    public void registerTaterzenType() {
        var builder = EntityType.Builder
                .of(TaterzenNPC::new, MobCategory.MISC)
                .sized(0.6F, 1.8F);
        var deferredRegister = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);

        var registryObject = deferredRegister.register(NPC_ID.getPath(), () -> builder.build(NPC_ID.getPath()));

        deferredRegister.register(FMLJavaModLoadingContext.get().getModEventBus());
        TATERZEN_TYPE = registryObject.get();
    }

    @Override
    public void openEditorGui(Player player) {
        SimpleGui editorGUI = createCommandGui((ServerPlayer) player, null, npcNode, Collections.singletonList("npc"), false);
        editorGUI.open();
    }
}
