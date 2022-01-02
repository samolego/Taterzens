package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;

import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class LockCommand {
    public static void registerNode(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> lockingNode = literal("action")
                .then(literal("lock")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.lock", config.perms.npcCommandPermissionLevel))
                        .executes(context -> lock(context, true))
                )
                .then(literal("unclock")
                        .requires(src -> permissions$checkPermission(src, "taterzens.npc.unlock", config.perms.npcCommandPermissionLevel))
                        .executes(context -> lock(context, false))
                )
                .build();

        npcNode.addChild(lockingNode);
    }

    private static int lock(CommandContext<CommandSourceStack> context, boolean lock) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Entity entity = source.getEntityOrException();
        return NpcCommand.selectedTaterzenExecutor(entity, taterzen -> {
            if (!taterzen.isLocked() && lock) {
                taterzen.setLocked(entity);
                source.sendSuccess(
                        successText("taterzens.command.lock.success", taterzen.getName().getString()),
                        false
                );
            } else if (taterzen.canEdit(entity) && !lock) {
                source.sendSuccess(
                        successText("taterzens.command.unlock.success", taterzen.getName().getString()),
                        false
                );
            }
        });
    }
}
