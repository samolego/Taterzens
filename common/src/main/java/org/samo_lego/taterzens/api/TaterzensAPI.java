package org.samo_lego.taterzens.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.api.professions.TaterzenProfession;
import org.samo_lego.taterzens.compatibility.LoaderSpecific;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.samo_lego.taterzens.Taterzens.*;

/**
 * Class containing static methods to use with Taterzens.
 */

// * IDK If I should put those in TaterzenNPC class instead,
// * but I feel like it would be too cluttered.
public class TaterzensAPI {

    private static final JsonParser parser = new JsonParser();
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .setLenient()
            .create();

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
                        CompoundTag compoundTag = (CompoundTag) tag;
                        TaterzenNPC taterzenNPC = new TaterzenNPC(TATERZEN_TYPE, world);
                        taterzenNPC.readCustomDataFromTag(compoundTag);
                        if(DISGUISELIB_LOADED && compoundTag.contains("CustomType")) {
                            CompoundTag customTypeTag = compoundTag.getCompound("CustomType");
                            Entity customEntity = EntityType.loadEntityWithPassengers(customTypeTag, taterzenNPC.world, (entityx) -> entityx);
                            LoaderSpecific.disguiselib$disguiseAs(taterzenNPC, customEntity);
                        }
                        return taterzenNPC;
                    }
                } catch(Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Saves {@link TaterzenNPC} to preset file.
     * @param taterzen taterzen to save.
     * @param preset file to save taterzen to.
     */
    public static void saveTaterzenToPreset(TaterzenNPC taterzen, File preset) {
        CompoundTag saveTag = new CompoundTag();
        taterzen.writeCustomDataToTag(saveTag);
        if(DISGUISELIB_LOADED && LoaderSpecific.disguiselib$isDisguised(taterzen)) {
            CompoundTag customTypeTag = new CompoundTag();
            taterzen.toTag(customTypeTag);
            if(customTypeTag.contains("DisguiseLib")) {
                // Saves DisguiseEntity to preset
                saveTag.put("CustomType", customTypeTag.getCompound("DisguiseLib").getCompound("DisguiseEntity"));
            }
        }

        //todo Weird as it is, those cannot be read back :(
        saveTag.remove("ArmorDropChances");
        saveTag.remove("HandDropChances");

        JsonElement element = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, saveTag);

        try(Writer writer = new OutputStreamWriter(new FileOutputStream(preset), StandardCharsets.UTF_8)) {
            gson.toJson(element, writer);
        } catch(IOException e) {
            getLogger().error("Problem occurred when saving Taterzen preset file: " + e.getMessage());
        }
    }

    /**
     * Creates a Taterzen NPC with rotations and custom name.
     * You'll still have to spawn it in (use {@link World#spawnEntity(Entity)}
     * to achieve that).
     * @param world Taterzen's world
     * @param displayName Taterzen's name.
     * @param pos Taterzen's position
     * @param rotations Taterzen's rotations (0 - head yaw, 1 - body yaw, 2 - pitch)
     * @return TaterzenNPC
     */
    public static TaterzenNPC createTaterzen(ServerWorld world, String displayName, Vec3d pos, float[] rotations) {
        TaterzenNPC taterzen = new TaterzenNPC(Taterzens.TATERZEN_TYPE, world);

        taterzen.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), rotations[1], rotations[2]);
        taterzen.setHeadYaw(rotations[0]);
        taterzen.setCustomName(new LiteralText(displayName));
        taterzen.applySkin(SkullBlockEntity.loadProperties(taterzen.getGameProfile()));

        return taterzen;
    }

    /**
     * Creates a Taterzen NPC from owner with provided display name.
     * You'll still have to spawn it in (use {@link World#spawnEntity(Entity)}
     * to achieve that).
     * @param owner player whose rotations and world will be copied to Taterzen
     * @param displayName Taterzen's name.
     * @return TaterzenNPC
     */
    public static TaterzenNPC createTaterzen(ServerPlayerEntity owner, String displayName) {
        return createTaterzen(owner.getServerWorld(), displayName, owner.getPos(), new float[]{owner.headYaw, owner.yaw, owner.pitch});
    }

    /**
     * Error text for no selected taterzen
     * @return formatted error text.
     */
    public static MutableText noSelectedTaterzenError() {
        return new LiteralText(lang.error.selectTaterzen)
                .formatted(Formatting.RED)
                .styled(style -> style
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(lang.showLoadedTaterzens)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/npc list"))
                );
    }

    /**
     * Registeres a new {@link TaterzenProfession}.
     *
     * @param prodessionId a unique id of profession
     * @param profession
     */
    public static void registerProfession(Identifier prodessionId, TaterzenProfession profession) {
        PROFESSION_TYPES.put(prodessionId, profession);
    }


    public static List<String> getPresets() {
        List<String> files = new ArrayList<>();
        Arrays.stream(presetsDir.listFiles()).forEach(file -> {
            if(file.isFile() && file.getName().endsWith(".json"))
                files.add(file.getName().substring(0, file.getName().length() - 5));
        });

        return files;
    }
}
