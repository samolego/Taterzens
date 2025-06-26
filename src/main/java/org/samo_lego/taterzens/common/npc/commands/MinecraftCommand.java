package org.samo_lego.taterzens.common.npc.commands;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.samo_lego.taterzens.common.npc.TaterzenNPC;

public class MinecraftCommand extends AbstractTaterzenCommand {
    private String command;

    public MinecraftCommand(String command) {
        super(CommandType.DEFAULT);
        this.command = command;
    }

    public MinecraftCommand() {
        super(CommandType.DEFAULT);
        this.command = "";
    }

    @Override
    public void execute(TaterzenNPC npc, Player player) {
        npc.getServer().getCommands().performPrefixedCommand(
                npc.createCommandSourceStackForNameResolution((ServerLevel) npc.level()), command.replaceAll(CLICKER_PLACEHOLDER, player.getGameProfile().getName()));
    }

    @Override
    public String toString() {
        return this.command;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag).putString("Command", this.command);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag cmdTag) {
        this.command = cmdTag.getString("Command").orElseThrow();
    }
}
