package org.samo_lego.taterzens.compatibility;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.Entity;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import xyz.nucleoid.disguiselib.casts.EntityDisguise;

/**
 * Just DisguiseLib methods in their own
 * class, in order to make
 * the mod run without the lib as well.
 */
public class DisguiseLibCompatibility {

    public static void disguiseAs(TaterzenNPC taterzen, Entity entity) {
        ((EntityDisguise) taterzen).disguiseAs(entity);
    }

    public static void setGameProfile(TaterzenNPC taterzen, GameProfile gameProfile) {
        ((EntityDisguise) taterzen).setGameProfile(gameProfile);
    }

    public static void clearDisguise(TaterzenNPC taterzen) {
        ((EntityDisguise) taterzen).removeDisguise();
    }
}
