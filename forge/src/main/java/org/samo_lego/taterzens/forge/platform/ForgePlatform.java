package org.samo_lego.taterzens.forge.platform;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegistryManager;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import org.samo_lego.taterzens.platform.Platform;
import org.samo_lego.taterzens.util.TextUtil;

import java.nio.file.Path;

import static org.samo_lego.taterzens.Taterzens.NPC_ID;
import static org.samo_lego.taterzens.Taterzens.TATERZEN_TYPE;
import static org.samo_lego.taterzens.forge.TaterzensForge.ENTITY_TYPE_REGISTER;

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
        TATERZEN_TYPE = ENTITY_TYPE_REGISTER.register(NPC_ID.getPath(), () -> EntityType.Builder
                .<TaterzenNPC>of(TaterzenNPC::new, MobCategory.MISC)
                .sized(0.6F, 1.8F)
                .build(NPC_ID.getPath()));
    }

    @Override
    public void clearDisguise(TaterzenNPC taterzen) {

    }

    @Override
    public void disguiseAs(TaterzenNPC taterzen, Entity entity) {

    }

    @Override
    public void openEditorGui(ServerPlayer player) {
        player.sendSystemMessage(TextUtil.translate("taterzens.gui.forge"));
    }
}
