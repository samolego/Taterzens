package org.samo_lego.taterzens.npc.commands;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.ArrayList;

public class CommandGroups extends ArrayList<ArrayList<AbstractTaterzenCommand>> {
    private int groupIndex;
    private final TaterzenNPC npc;

    public CommandGroups(TaterzenNPC npc) {
        this.npc = npc;
        this.groupIndex = 0;
    }

    public boolean addCommand(AbstractTaterzenCommand command) {
        return this.get(this.groupIndex).add(command);
    }

    @Override
    public void clear() {
        super.clear();
        this.groupIndex = 0;
    }

    public void execute(ServerPlayer player) {
        if (this.groupIndex >= this.size()) {
            this.groupIndex = 0;
        }

        var commands = ImmutableList.copyOf(this.get(this.groupIndex));
        for (var cmd : commands) {
            cmd.execute(this.npc, player);
        }
        ++this.groupIndex;
    }

    public void toTag(CompoundTag tag) {
        tag.putInt("GroupIndex", this.groupIndex);
        var commands = new ListTag();

        for (int i = 0; i < this.size(); ++i) {
            var cmds = this.get(i);
            ListTag cmdList = new ListTag();
            cmds.forEach(cmd -> cmdList.add(cmd.toTag(new CompoundTag())));
            commands.add(cmdList);
        }
        tag.put("Contents", commands);
    }

    public void fromTag(CompoundTag tag) {
        this.groupIndex = tag.getInt("GroupIndex");

        ListTag cmdsArray = (ListTag) tag.get("Contents");
        if (cmdsArray != null) {
            for (var cmds : cmdsArray) {
                var cmdList = new ArrayList<AbstractTaterzenCommand>();
                for (var cmd : (ListTag) cmds) {
                    var cmdTag = (CompoundTag) cmd;

                    AbstractTaterzenCommand toAdd;
                    if (AbstractTaterzenCommand.CommandType.valueOf(cmdTag.getString("Type")) == AbstractTaterzenCommand.CommandType.BUNGEE) {
                        toAdd = new BungeeCommand();
                    } else {
                        toAdd = new MinecraftCommand();
                    }
                    toAdd.fromTag(cmdTag);
                    cmdList.add(toAdd);
                }
                this.add(cmdList);
            }
        }
    }

    @Deprecated
    public void fromOldTag(ListTag minecraftCommands, ListTag bungeeCommands) {
        this.groupIndex = 0;

        // Commands
        var cmds = new ArrayList<AbstractTaterzenCommand>();
        if (minecraftCommands != null) {
            minecraftCommands.forEach(cmdTag -> cmds.add(new MinecraftCommand(cmdTag.getAsString())));
        }

        // Bungee commands
        if (bungeeCommands != null) {
            bungeeCommands.forEach(cmdTag -> {
                ListTag cmdList = (ListTag) cmdTag;
                String command = cmdList.get(0).getAsString();
                String player = cmdList.get(1).getAsString();
                String argument = cmdList.get(2).getAsString();

                cmds.add(new BungeeCommand(BungeeCommand.BungeeMessage.valueOf(command), player, argument));
            });
        }
        this.add(cmds);
    }
}
