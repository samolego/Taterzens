package org.samo_lego.taterzens.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.api.professions.TaterzenProfession;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.samo_lego.taterzens.Taterzens.GSON;
import static org.samo_lego.taterzens.Taterzens.LOGGER;
import static org.samo_lego.taterzens.Taterzens.MOD_ID;
import static org.samo_lego.taterzens.Taterzens.PROFESSION_TYPES;

/**
 * Class containing static methods to use with Taterzens.
 */

// * IDK If I should put those in TaterzenNPC class instead,
// * but I feel like it would be too cluttered.
public class TaterzensAPI {

    /**
     * Loads {@link TaterzenNPC} from preset.
     *
     * @param preset preset file containing Taterzen. Should be json.
     * @param world world of Taterzen.
     * @return TaterzenNPC
     */
    @Nullable
    public static TaterzenNPC loadTaterzenFromPreset(File preset, Level world) {
        if (preset.exists()) {
            String name = preset.getName();
            TaterzenNPC taterzenNPC = new TaterzenNPC(world);
            taterzenNPC.loadFromPresetFile(preset, name.substring(0, name.lastIndexOf('.')));

            return taterzenNPC;
        }

        return null;
    }

    /**
     * Gets the Taterzen data from file.
     * @param preset preset file of Taterzen.
     * @return CompoundTag containing Taterzen data.
     */
    public static CompoundTag loadPresetTag(File preset) {
        JsonElement element = null;
        try(BufferedReader fileReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(preset), StandardCharsets.UTF_8)
        )
        ) {
            element = JsonParser.parseReader(fileReader).getAsJsonObject();
        } catch(IOException e) {
            LOGGER.error(MOD_ID + " Problem occurred when trying to load Taterzen preset: ", e);
        }
        if(element != null) {
            try {
                Tag tag = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, element);
                if(tag instanceof CompoundTag saveTag) {
                    return saveTag;
                }
            } catch(Throwable e) {
                e.printStackTrace();
            }
        }
        return new CompoundTag();
    }

    /**
     * Saves {@link TaterzenNPC} to preset file.
     * @param taterzen taterzen to save.
     * @param preset file to save taterzen to.
     */
    public static void saveTaterzenToPreset(TaterzenNPC taterzen, File preset) {
        CompoundTag saveTag = new CompoundTag();
        taterzen.saveWithoutId(saveTag);

        // Weird as it is, those cannot be read back :(
        saveTag.remove("ArmorDropChances");
        saveTag.remove("HandDropChances");

        // We want a new UUID and other stuff below
        saveTag.remove("UUID");
        saveTag.remove("Pos");
        saveTag.remove("Motion");
        saveTag.remove("Rotation");

        // Saving team
        Team team = taterzen.getTeam();
        if (team != null) {
            String teamName = team.getName();
            CompoundTag npcTag = (CompoundTag) saveTag.get("TaterzenNPCTag");
            if (npcTag != null)
                npcTag.putString("SavedTeam", teamName);
        }

        JsonElement element = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, saveTag);

        try(Writer writer = new OutputStreamWriter(new FileOutputStream(preset), StandardCharsets.UTF_8)) {
            GSON.toJson(element, writer);
        } catch(IOException e) {
            LOGGER.error("Problem occurred when saving Taterzen preset file: " + e.getMessage());
        }
    }

    /**
     * Creates a Taterzen NPC with rotations and custom name.
     * You'll still have to spawn it in (use {@link Level#addFreshEntity(Entity)}
     * to achieve that).
     * @param world Taterzen's world
     * @param displayName Taterzen's name.
     * @param pos Taterzen's position
     * @param rotations Taterzen's rotations (0 - head yaw, 1 - body yaw, 2 - pitch)
     * @return TaterzenNPC
     */
    public static TaterzenNPC createTaterzen(ServerLevel world, String displayName, Vec3 pos, float[] rotations) {
        TaterzenNPC taterzen = new TaterzenNPC(world);

        taterzen.moveTo(pos.x(), pos.y(), pos.z(), rotations[1], rotations[2]);
        taterzen.setYHeadRot(rotations[0]);
        taterzen.setCustomName(Component.literal(displayName));
        SkullBlockEntity.updateGameprofile(taterzen.getGameProfile(), taterzen::applySkin);

        return taterzen;
    }

    /**
     * Creates a Taterzen NPC from owner with provided display name.
     * You'll still have to spawn it in (use {@link Level#addFreshEntity(Entity)}
     * to achieve that).
     * @param owner player whose rotations and world will be copied to Taterzen
     * @param displayName Taterzen's name.
     * @return TaterzenNPC
     */
    public static TaterzenNPC createTaterzen(ServerPlayer owner, String displayName) {
        return createTaterzen(owner.getLevel(), displayName, owner.position(), new float[]{owner.yHeadRot, owner.getYRot(), owner.getXRot()});
    }

    /**
     * Registers a new {@link TaterzenProfession}.
     * @param professionId a unique id of profession.
     * @param professionInitilizer constructor of profession that accepts {@link TaterzenNPC}.
     */
    public static void registerProfession(ResourceLocation professionId, Function<TaterzenNPC, TaterzenProfession> professionInitilizer) {
        if (!PROFESSION_TYPES.containsKey(professionId)) {
            PROFESSION_TYPES.put(professionId, professionInitilizer);
        } else {
            LOGGER.warn("[Taterzens] A mod {} tried to register the profession {} which is already present. Ignoring.", professionId.getNamespace(), professionId.getPath());
        }
    }


    public static List<String> getPresets() {
        List<String> files = new ArrayList<>();
        File[] presets = Taterzens.getInstance().getPresetDirectory().listFiles();
        if(presets != null) {
            final String ending = ".json";
            for(File preset : presets) {
                if(preset.isFile() && preset.getName().endsWith(ending))
                    files.add(preset.getName().substring(0, preset.getName().length() - ending.length()));
            }
        }
        return files;
    }
}
