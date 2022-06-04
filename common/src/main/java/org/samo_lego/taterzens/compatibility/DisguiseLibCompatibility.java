package org.samo_lego.taterzens.compatibility;

import net.minecraft.world.entity.Entity;
import org.samo_lego.taterzens.npc.TaterzenNPC;
import xyz.nucleoid.disguiselib.api.EntityDisguise;

/**
 * Just DisguiseLib methods in their own
 * class, in order to make
 * the mod run without the lib as well.
 */
public class DisguiseLibCompatibility {

    public static void disguiseAs(TaterzenNPC taterzen, Entity entity) {
        ((EntityDisguise) taterzen).disguiseAs(entity);
    }

    public static void clearDisguise(TaterzenNPC taterzen) {
        ((EntityDisguise) taterzen).removeDisguise();
    }
}
