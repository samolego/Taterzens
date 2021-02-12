package org.samo_lego.taterzens.api;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.io.File;

/**
 * Class containing static methods to use with Taterzens.
 */
public class TaterzensAPI {

    /**
     * Loads {@link TaterzenNPC} from preset.
     * @param preset preset file containing Taterzen. Should be json.
     * @param owner o
     * @return TaterzenNPC
     */
    @Nullable
    public static TaterzenNPC loadTaterzenFromPreset(File preset, ServerPlayerEntity owner) {
        /*if(preset.exists()) {
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
                        TaterzenNPC taterzenNPC = new TaterzenNPC(world);
                        taterzenNPC.readCustomDataFromTag((CompoundTag) tag);
                        taterzenNPC.sendProfileUpdates();


                    }
                } catch(Throwable e) {
                    e.printStackTrace();
                }
            }
        } else*/
            return null;
    }
}
