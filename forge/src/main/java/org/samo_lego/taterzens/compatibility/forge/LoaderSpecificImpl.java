package org.samo_lego.taterzens.compatibility.forge;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import org.samo_lego.taterzens.npc.TaterzenNPC;

public class LoaderSpecificImpl {

    public static void disguiselib$setGameProfile(TaterzenNPC taterzenNPC, GameProfile gameProfile) {
    }

    public static void disguiselib$disguiseAs(TaterzenNPC taterzenNPC, EntityType<?> entityType) {
    }

    public static void disguiselib$disguiseAs(TaterzenNPC taterzenNPC, Entity entity) {
    }

    public static boolean disguiselib$isDisguised(TaterzenNPC taterzen) {
        return false;
    }

    public static boolean permissions$checkPermission(ServerCommandSource source, String permissionNode) {
        return true;
    }

    public static void disguiselib$clearDisguise(TaterzenNPC taterzen) {
    }
}
