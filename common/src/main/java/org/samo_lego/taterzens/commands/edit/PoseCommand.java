package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.Pose;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.commands.NpcCommand;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class PoseCommand {
    public static void registerNode(LiteralCommandNode<CommandSourceStack> editNode) {
        LiteralCommandNode<CommandSourceStack> poseNode = literal("pose")
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.pose", config.perms.npcCommandPermissionLevel))
                .then(argument("pose name", word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(Stream.of(Pose.values()).map(Enum::name).collect(Collectors.toList()), builder))
                        .executes(PoseCommand::editPose)
                )
                .build();

        editNode.addChild(poseNode);
    }

    private static int editPose(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack src = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(src.getEntityOrException(), taterzen -> {
            String pose = StringArgumentType.getString(context, "pose name");
            taterzen.setPose(Pose.valueOf(pose));
            src.sendSuccess(successText("taterzens.command.pose", pose), false);
        });
    }
}
