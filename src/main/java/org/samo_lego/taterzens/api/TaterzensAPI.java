package org.samo_lego.taterzens.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import xyz.nucleoid.disguiselib.EntityDisguise;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.samo_lego.taterzens.Taterzens.*;

/**
 * Class containing static methods to use with Taterzens.
 */

// * IDK If I should put those in TaterzenNPC class instead,
// * cause I feel like it would be too cluttered.
public class TaterzensAPI {

    private static final JsonParser parser = new JsonParser();

    /**
     * Loads {@link TaterzenNPC} from preset.
     *
     * @param preset preset file containing Taterzen. Should be json.
     * @param world world of Taterzen.
     * @return TaterzenNPC
     */
    @Nullable
    public static TaterzenNPC loadTaterzenFromPreset(File preset, World world) {
        if(preset.exists()) {
            JsonElement element = null;
            try(BufferedReader fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(preset), StandardCharsets.UTF_8)
            )
            ) {
                element = parser.parse(fileReader).getAsJsonObject();
            } catch(IOException e) {
                Taterzens.getLogger().error(MODID + " Problem occurred when trying to load Taterzen preset: ", e);
            }
            if(element != null) {
                try {
                    Tag tag = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, element);
                    if(tag instanceof CompoundTag) {
                        TaterzenNPC taterzenNPC = new TaterzenNPC(TATERZEN, world);
                        taterzenNPC.readCustomDataFromTag((CompoundTag) tag);
                        return taterzenNPC;
                    }
                } catch(Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void saveTaterzenToPreset(TaterzenNPC taterzen, File preset) {
        CompoundTag saveTag = new CompoundTag();
        taterzen.writeCustomDataToTag(saveTag);
        saveTag.putString("entityType", Registry.ENTITY_TYPE.getId(((EntityDisguise) taterzen).getDisguiseType()).toString());
        //todo Weird as it is, those cannot be read back :(
        saveTag.remove("ArmorDropChances");
        saveTag.remove("HandDropChances");

        TATERZEN_NPCS.add(taterzen); // When writing to tag, it was removed so we add it back

        JsonElement element = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, saveTag);

        try(Writer writer = new OutputStreamWriter(new FileOutputStream(preset), StandardCharsets.UTF_8)) {
            writer.write(element.toString());
        } catch(IOException e) {
            getLogger().error("Problem occurred when saving Taterzen preset file: " + e.getMessage());
        }
    }
}
