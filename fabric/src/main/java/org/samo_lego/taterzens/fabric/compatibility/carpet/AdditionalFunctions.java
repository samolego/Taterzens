package org.samo_lego.taterzens.fabric.compatibility.carpet;

import carpet.script.annotation.AnnotationParser;
import carpet.script.annotation.ScarpetFunction;
import carpet.script.value.EntityValue;
import carpet.script.value.NullValue;
import carpet.script.value.Value;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.api.TaterzensAPI;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

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
    @ScarpetFunction(maxParams = 2)
    public EntityValue spawn_taterzen(ServerPlayer player, String name) {
        return (EntityValue) EntityValue.of(TaterzensAPI.createTaterzen(player, name));
    }

    /**
     * Gets player's selected taterzen.
     * @param player player to get taterzen from.
     * @return taterzen of player or null if player doesn't have taterzen selected.
     */
    @ScarpetFunction(maxParams = 1)
    public Value players_taterzen(ServerPlayer player) {
        TaterzenNPC npc = ((ITaterzenEditor) player).getNpc();
        if (npc != null)
            return EntityValue.of(npc);
        return NullValue.NULL;
    }

    /**
     * Returns a taterzen from {{@link org.samo_lego.taterzens.Taterzens#TATERZEN_NPCS}} by its id.
     * @param id id of taterzen to get.
     * @return taterzen with given id or null if taterzen with given id doesn't exist.
     */
    @ScarpetFunction(maxParams = 1)
    public Value taterzen_by_id(int id) {
        // Check size of TATERZEN_NPCS
        if (id < Taterzens.TATERZEN_NPCS.size()) {
            return EntityValue.of((TaterzenNPC) Taterzens.TATERZEN_NPCS.toArray()[id]);
        }
        return NullValue.NULL;
    }
}
