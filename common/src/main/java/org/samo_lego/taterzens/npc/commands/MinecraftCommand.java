package org.samo_lego.taterzens.npc.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.samo_lego.taterzens.npc.TaterzenNPC;

public class MinecraftCommand extends AbstractTaterzenCommand {
    private String command;
    private CommandExecutor executor;

    public MinecraftCommand(String command, CommandExecutor executor) {
        super(CommandType.DEFAULT);
        this.command = command;
        this.executor = executor;
    }

    public MinecraftCommand(String command) {
        super(CommandType.DEFAULT);
        this.command = command;
        this.executor = CommandExecutor.TARTERZEN;
    }

    public MinecraftCommand() {
        super(CommandType.DEFAULT);
        this.command = "";
        this.executor = CommandExecutor.TARTERZEN;
    }

    @Override
    public void execute(TaterzenNPC npc, Player player) {
        CommandSourceStack source = executor == CommandExecutor.TARTERZEN ?
                npc.createCommandSourceStack() : player.createCommandSourceStack();
        npc.getServer().getCommands().performPrefixedCommand(source, command.replaceAll(CLICKER_PLACEHOLDER, player.getGameProfile().getName()));
    }

    @Override
    public String toString() {
        return this.command;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putString("Command", this.command);
        if (executor != null)
            tag.putString("Executor", this.executor.name());
        return tag;
    }

    @Override
    public void fromTag(CompoundTag cmdTag) {
        this.command = cmdTag.getString("Command");
        if (cmdTag.contains("Executor"))
            this.executor = CommandExecutor.valueOf(cmdTag.getString("Executor"));
    }

    public enum CommandExecutor {
        TARTERZEN("taterzen"),
        PLAYER("player");

        private final String argName;

        CommandExecutor(String argName) {
            this.argName = argName;
        }

        public String getArgName() {
            return argName;
        }
    }
}
