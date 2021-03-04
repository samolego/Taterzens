package org.samo_lego.taterzens.compatibility;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityType;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import xyz.nucleoid.disguiselib.EntityDisguise;

public class DisguiseLibCompatibility {

    public static void disguiseAs(TaterzenNPC taterzen, EntityType<?> entityType) {
        ((EntityDisguise) taterzen).disguiseAs(entityType); //todo
    }

    public static void setGameProfile(TaterzenNPC taterzen, GameProfile gameProfile) {
        ((EntityDisguise) taterzen).setGameProfile(gameProfile);
    }
}
