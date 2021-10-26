package org.samo_lego.taterzens.compatibility.carpet;

import carpet.CarpetServer;
import carpet.script.CarpetEventServer;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.EntityValue;
import carpet.script.value.Value;
import carpet.script.value.ValueConversions;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.ArrayList;
import java.util.List;

public class TaterzenScarpetEvent extends CarpetEventServer.Event {
    public TaterzenScarpetEvent(String name, int reqArgs) {
        super(name, reqArgs, true);
    }

    public void triggerCustomEvent(@NotNull TaterzenNPC taterzen, Object... args) {
        if (handler.reqArgs != args.length + 1)
            throw new InternalExpressionException("Expected " + handler.reqArgs + " arguments for " + name + ", got " + args.length + 1);

        handler.call(
                () -> {
                    List<Value> valArgs = new ArrayList<>();
                    valArgs.add(new EntityValue(taterzen));
                    for (Object o: args) {
                        valArgs.add(ValueConversions.guess((ServerLevel) taterzen.level, o));
                    }
                    return valArgs;
                },
                () -> CarpetServer.minecraft_server.createCommandSourceStack().withLevel((ServerLevel) taterzen.level)
        );
    }
}
