package org.samo_lego.taterzens.npc.commands;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.samo_lego.taterzens.npc.TaterzenNPC;

public abstract class AbstractTaterzenCommand {
    public static final String CLICKER_PLACEHOLDER = "--clicker--";
    private CommandType type;

    public AbstractTaterzenCommand(CommandType type) {
        this.type = type;
    }

    public abstract void execute(TaterzenNPC npc, Player player);

    public CommandType getType() {
        return this.type;
    }

    @Override
    public abstract String toString();

    public CompoundTag toTag(CompoundTag tag) {
        tag.putString("Type", this.type.toString());
        return tag;
    }

    public abstract void fromTag(CompoundTag cmdTag);

    public enum CommandType {
        BUNGEE,
        DEFAULT,
    }
}
