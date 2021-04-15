package org.samo_lego.taterzens.compatibility;

import com.mojang.authlib.GameProfile;
import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import org.samo_lego.taterzens.npc.TaterzenNPC;

/**
 * Methods that are further redirected to forge / fabric implementations.
 */
public class LoaderSpecific {
    @ExpectPlatform
    public static void disguiselib$setGameProfile(TaterzenNPC taterzenNPC, GameProfile gameProfile) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void disguiselib$disguiseAs(TaterzenNPC taterzenNPC, EntityType<?> entityType) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static void disguiselib$disguiseAs(TaterzenNPC taterzenNPC, Entity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean disguiselib$isDisguised(TaterzenNPC taterzen) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean permissions$checkPermission(ServerCommandSource source, String permissionNode) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void disguiselib$clearDisguise(TaterzenNPC taterzen) {
        throw new AssertionError();
    }
}
