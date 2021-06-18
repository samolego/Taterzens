package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityPose;
import net.minecraft.server.command.ServerCommandSource;
import org.samo_lego.taterzens.commands.NpcCommand;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.PERMISSIONS;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class PoseCommand {
    public static void registerNode(LiteralCommandNode<ServerCommandSource> editNode) {
        LiteralCommandNode<ServerCommandSource> poseNode = literal("pose")
                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_pose, config.perms.npcCommandPermissionLevel))
                .then(argument("pose name", word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(Stream.of(EntityPose.values()).map(Enum::name).collect(Collectors.toList()), builder))
                        .executes(PoseCommand::editPose)
                )
                .build();

        editNode.addChild(poseNode);
    }

    private static int editPose(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource src = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(src.getPlayer(), taterzen -> {
            String pose = StringArgumentType.getString(context, "pose name");
            taterzen.setPose(EntityPose.valueOf(pose));
            src.sendFeedback(successText("taterzens.command.pose", pose), false);
        });
    }
}
