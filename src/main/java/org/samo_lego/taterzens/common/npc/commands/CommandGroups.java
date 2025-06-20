package org.samo_lego.taterzens.common.npc.commands;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.samo_lego.taterzens.common.npc.TaterzenNPC;

import java.util.ArrayList;
import java.util.List;

public class CommandGroups extends ArrayList<ArrayList<AbstractTaterzenCommand>> {
    private int groupIndex;
    private final TaterzenNPC npc;

    public CommandGroups(TaterzenNPC npc) {
        this.npc = npc;
        this.groupIndex = 0;
    }

    public boolean addCommand(AbstractTaterzenCommand command) {
        if (this.groupIndex >= this.size()) {
            this.add(new ArrayList<>());
        }
        return this.get(this.groupIndex).add(command);
    }

    @Override
    public void clear() {
        super.clear();
        this.groupIndex = 0;
    }

    public void execute(ServerPlayer player) {
        if (this.isEmpty()) {
            return;
        }

        if (this.groupIndex >= this.size()) {
            this.groupIndex = 0;
        }

        var commands = ImmutableList.copyOf(this.get(this.groupIndex));
        for (var cmd : commands) {
            cmd.execute(this.npc, player);
        }
        ++this.groupIndex;
    }

    public void toTag(ValueOutput tag) {
        tag.putInt("GroupIndex", this.groupIndex);
        var commands = tag.list("Contents", CompoundTag.CODEC.listOf());

        for (int i = 0; i < this.size(); ++i) {
            var cmds = this.get(i);
            List<CompoundTag> cmdList = new ArrayList<>();
            cmds.forEach(cmd -> cmdList.add(cmd.toTag(new CompoundTag())));
            commands.add(cmdList);
        }
    }

    public void fromTag(ValueInput tag) {
        this.groupIndex = tag.getInt("GroupIndex").orElseThrow();

        tag.list("Contents", CompoundTag.CODEC.listOf()).ifPresent(cmdsArray -> {
            for (var cmds : cmdsArray) {
                var cmdList = new ArrayList<AbstractTaterzenCommand>();
                for (var cmdTag : cmds) {
                    AbstractTaterzenCommand toAdd;
                    if (AbstractTaterzenCommand.CommandType.valueOf(cmdTag.getString("Type").orElseThrow()) == AbstractTaterzenCommand.CommandType.BUNGEE) {
                        toAdd = new BungeeCommand();
                    } else {
                        toAdd = new MinecraftCommand();
                    }
                    toAdd.fromTag(cmdTag);
                    cmdList.add(toAdd);
                }
                this.add(cmdList);
            }
        });
    }

    @Deprecated
    public void fromOldTag(Iterable<String> minecraftCommands, Iterable<List<String>> bungeeCommands) {
        this.groupIndex = 0;

        // Commands
        var cmds = new ArrayList<AbstractTaterzenCommand>();
        if (minecraftCommands != null) {
            minecraftCommands.forEach(cmdTag -> cmds.add(new MinecraftCommand(cmdTag)));
        }

        // Bungee commands
        if (bungeeCommands != null) {
            bungeeCommands.forEach(cmdList -> {
                String command = cmdList.get(0);
                String player = cmdList.get(1);
                String argument = cmdList.get(2);

                cmds.add(new BungeeCommand(BungeeCommand.BungeeMessage.valueOf(command), player, argument));
            });
        }
        this.add(cmds);
    }

    public int createGroup() {
        this.add(new ArrayList<>());
        this.groupIndex = this.size() - 1;
        return this.groupIndex;
    }
}
