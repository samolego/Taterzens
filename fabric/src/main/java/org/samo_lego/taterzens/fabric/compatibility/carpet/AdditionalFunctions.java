package org.samo_lego.taterzens.fabric.compatibility.carpet;

import carpet.script.annotation.AnnotationParser;
import carpet.script.annotation.ScarpetFunction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.api.TaterzensAPI;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.UUID;

public class AdditionalFunctions {

    /**
     * Register scarpet functions.
     */
    public static void init() {
        AnnotationParser.parseFunctionClass(AdditionalFunctions.class);
    }

    /**
     * Creates a new taterzen at coordinates of the provided player.
     * @param player player to create taterzen at.
     * @param name name of taterzen to create.
     * @return created taterzen.
     */
    @ScarpetFunction
    public Entity spawn_taterzen(ServerPlayer player, String name) {
        TaterzenNPC npc = TaterzensAPI.createTaterzen(player, name);
        player.getLevel().addFreshEntity(npc);
        return npc;
    }

    /**
     * Gets player's selected taterzen.
     * @param player player to get taterzen from.
     * @return taterzen of player or null if player doesn't have taterzen selected.
     */
    @ScarpetFunction
    public Entity players_taterzen(ServerPlayer player) {
        return ((ITaterzenEditor) player).getNpc();
    }

    /**
     * Returns a taterzen from {{@link org.samo_lego.taterzens.Taterzens#TATERZEN_NPCS}} by its id.
     * @param id id of taterzen to get.
     * @return taterzen with given id or null if taterzen with given id doesn't exist.
     */
    @ScarpetFunction
    public Entity taterzen_by_id(int id) {
        // Check size of TATERZEN_NPCS
        if (id < Taterzens.TATERZEN_NPCS.size()) {
            return (TaterzenNPC) Taterzens.TATERZEN_NPCS.values().toArray()[id];
        }
        return null;
    }

    @ScarpetFunction
    public Entity taterzen_by_uuid(String uuid) {
        return Taterzens.TATERZEN_NPCS.get(UUID.fromString(uuid));
    }

    @ScarpetFunction
    public Entity taterzen_by_name(String name) {
        for (TaterzenNPC npc : Taterzens.TATERZEN_NPCS.values()) {
            if (npc.getName().getString().equals(name)) {
                return npc;
            }
        }
        return null;
    }
}
