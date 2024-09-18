package org.samo_lego.taterzens.fabric.platform;

import eu.pb4.sgui.api.gui.SimpleGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.samo_lego.taterzens.common.npc.TaterzenNPC;
import org.samo_lego.taterzens.common.platform.Platform;
import org.samo_lego.taterzens.fabric.mixin.AMappedRegistry;

import java.nio.file.Path;
import java.util.Collections;

import static org.samo_lego.taterzens.common.Taterzens.NPC_ID;
import static org.samo_lego.taterzens.common.Taterzens.TATERZEN_TYPE;
import static org.samo_lego.taterzens.common.commands.NpcCommand.npcNode;
import static org.samo_lego.taterzens.fabric.gui.EditorGUI.createCommandGui;

public class FabricPlatform implements Platform {

    private static final int REGISTRY_ITEMS_SIZE = ((AMappedRegistry<?>) BuiltInRegistries.ITEM).getById().size();

    @Override
    public Path getConfigDirPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public int getItemRegistrySize() {
        return REGISTRY_ITEMS_SIZE;
    }

    /**
     * Checks permission of commandSource using Lucko's
     * permission API.
     * If permission isn't set, it will require the commandSource
     * to have permission level set in the config.
     *
     * @param commandSource commandSource to check permission for.
     * @param permission permission node to check.
     * @param fallbackLevel level to require if permission isn't set
     * @return true if commandSource has the permission, otherwise false
     */
    @Override
    public boolean checkPermission(CommandSourceStack commandSource, String permission, int fallbackLevel) {
        // Enable command blocks, therefore null check
        return commandSource.getEntity() == null || Permissions.check(commandSource, permission, fallbackLevel);
    }


    @Override
    public void registerTaterzenType() {
        final EntityType<TaterzenNPC> type = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                NPC_ID,
                FabricEntityTypeBuilder
                        .<TaterzenNPC>create(MobCategory.MISC, TaterzenNPC::new)
                        .dimensions(EntityDimensions.scalable(0.6F, 1.8F))
                        .build());

        TATERZEN_TYPE = () -> type;
    }

    @Override
    public void openEditorGui(ServerPlayer player) {
        // Commenting out since we've disabled the Editor GUI for now with the 1.21 update
        SimpleGui editorGUI = createCommandGui(player, null, npcNode, Collections.singletonList("npc"), false);
        editorGUI.open();
    }

    @Override
    public void disguiseAs(TaterzenNPC taterzen, Entity entity) {
        //todo ((EntityDisguise) taterzen).disguiseAs(entity);
    }

    @Override
    public void clearDisguise(TaterzenNPC taterzen) {
        //todo ((EntityDisguise) taterzen).removeDisguise();
    }

}
