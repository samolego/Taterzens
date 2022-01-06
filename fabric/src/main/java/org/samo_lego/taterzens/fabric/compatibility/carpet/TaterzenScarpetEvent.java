package org.samo_lego.taterzens.fabric.compatibility.carpet;

import carpet.CarpetServer;
import carpet.script.CarpetEventServer;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.EntityValue;
import carpet.script.value.ListValue;
import carpet.script.value.Value;
import carpet.script.value.ValueConversions;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TaterzenScarpetEvent extends CarpetEventServer.Event {
    public TaterzenScarpetEvent(String name, int reqArgs) {
        super(name, reqArgs, true);
    }

    public void triggerCustomEvent(@NotNull TaterzenNPC taterzen, HashSet<Value> traits, Object... args) {
        if (handler.reqArgs != args.length + 2)
            throw new InternalExpressionException("Expected " + handler.reqArgs + " arguments for " + name + ", got " + (args.length + 1));

        handler.call(
                () -> {
                    List<Value> valArgs = new ArrayList<>();
                    valArgs.add(EntityValue.of(taterzen));
                    valArgs.add(ListValue.wrap(traits.stream()));
                    for (Object o: args) {
                        valArgs.add(ValueConversions.guess((ServerLevel) taterzen.level, o));
                    }
                    return valArgs;
                },
                () -> CarpetServer.minecraft_server.createCommandSourceStack().withLevel((ServerLevel) taterzen.level)
        );
    }
}
