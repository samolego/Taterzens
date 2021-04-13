package org.samo_lego.taterzens.compatibility.fabric;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import org.samo_lego.taterzens.api.professions.ProfessionParseCallback;
import org.samo_lego.taterzens.compatibility.DisguiseLibCompatibility;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import static org.samo_lego.taterzens.compatibility.PermissionHelper.checkPermission;

public class LoaderSpecificImpl {

    public static void disguiselib$setGameProfile(TaterzenNPC taterzenNPC, GameProfile gameProfile) {
        DisguiseLibCompatibility.setGameProfile(taterzenNPC, gameProfile);
    }

    public static void parseProfessionEvent(String professionType, TaterzenNPC taterzenNPC) {
        ProfessionParseCallback.EVENT.invoker().parseProfession(professionType, taterzenNPC);
    }

    public static void disguiselib$disguiseAs(TaterzenNPC taterzenNPC, EntityType<?> entityType) {
        DisguiseLibCompatibility.disguiseAs(taterzenNPC, entityType);
    }

    public static void disguiselib$disguiseAs(TaterzenNPC taterzenNPC, Entity entity) {
        DisguiseLibCompatibility.disguiseAs(taterzenNPC, entity);
    }

    public static boolean disguiselib$isDisguised(TaterzenNPC taterzen) {
        return DisguiseLibCompatibility.isDisguised(taterzen);
    }

    public static boolean permissions$checkPermission(ServerCommandSource source, String permissionNode) {
        return checkPermission(source, permissionNode);
    }

    public static void disguiselib$clearDisguise(TaterzenNPC taterzen) {
        DisguiseLibCompatibility.clearDisguise(taterzen);
    }
}
