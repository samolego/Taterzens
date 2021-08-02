package org.samo_lego.taterzens.commands.edit;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.util.Formatting;
import org.samo_lego.taterzens.commands.NpcCommand;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.FABRICTAILOR_LOADED;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.mixin.accessors.PlayerEntityAccessor.getPLAYER_MODEL_PARTS;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class SkinCommand {

    private static final String MINESKIN_API_URL = "https://api.mineskin.org/get/id/";
    private static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();

    public static void registerNode(LiteralCommandNode<ServerCommandSource> editNode) {
        LiteralCommandNode<ServerCommandSource> skinNode = literal("skin")
                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.skin", config.perms.npcCommandPermissionLevel))
                .then(argument("mineskin URL | playername", message())
                        .executes(SkinCommand::setCustomSkin)
                )
                .executes(SkinCommand::copySkinLayers)
                .build();

        editNode.addChild(skinNode);
    }

    private static int setCustomSkin(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String id = MessageArgumentType.getMessage(context, "mineskin URL | playername").getString();

        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrThrow(), taterzen -> {
            // Shameless self-promotion
            if(config.fabricTailorAdvert) {
                if(FABRICTAILOR_LOADED) {
                    source.sendFeedback(translate("advert.fabrictailor.skin_command")
                                    .formatted(Formatting.GOLD)
                                    .styled(style ->
                                            style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skin set"))
                                    ),
                            false
                    );
                } else {
                    source.sendFeedback(translate("advert.fabrictailor")
                                    .formatted(Formatting.ITALIC)
                                    .formatted(Formatting.GOLD)
                                    .styled(style -> style
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/FabricTailor"))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("advert.tooltip.install", "FabricTailor")))
                                    ),
                            false
                    );
                }

            }

            if(id.contains(":") ) {
                THREADPOOL.submit(() -> {
                    String param = id.replaceAll("[^0-9]", "");
                    String mineskinUrl = MINESKIN_API_URL + param;
                    try {
                        URL url = new URL(mineskinUrl);
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                        connection.setUseCaches(false);
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestMethod("GET");

                        try (
                                InputStream is = connection.getInputStream();
                                InputStreamReader isr = new InputStreamReader(is);
                                BufferedReader br = new BufferedReader(isr)
                        ) {
                            String response = br.readLine();
                            String value = response.split("\"value\":\"")[1].split("\"")[0];
                            String signature = response.split("\"signature\":\"")[1].split("\"")[0];

                            NbtCompound skinTag = new NbtCompound();
                            skinTag.putString("value", value);
                            skinTag.putString("signature", signature);

                            taterzen.setSkinFromTag(skinTag);
                            taterzen.sendProfileUpdates();

                            source.sendFeedback(
                                    successText("taterzens.command.skin.fetched", id),
                                    false
                            );
                        }
                    } catch(MalformedURLException e) {
                        source.sendError(errorText("taterzens.error.invalid.url", mineskinUrl));
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                // Player's name
                GameProfile skinProfile = new GameProfile(null, id);
                SkullBlockEntity.loadProperties(skinProfile, taterzen::applySkin);
                context.getSource().sendFeedback(
                        successText("taterzens.command.skin.fetched", id),
                        false
                );
            }
        });
    }


    private static int copySkinLayers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        return NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
            Byte skinLayers = player.getDataTracker().get(getPLAYER_MODEL_PARTS());
            taterzen.setSkinLayers(skinLayers);

            taterzen.sendProfileUpdates();
            source.sendFeedback(
                    successText("taterzens.command.skin.mirrored", taterzen.getName().getString()),
                    false
            );
        });
    }
}
