package org.samo_lego.taterzens.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextUtil {
    private static final JsonParser parser = new JsonParser();

    /**
     * Inserts colored name of Taterzen in string message.
     *
     * @param message message to insert name in.
     * @param messageColor color format of the message.
     * @param taterzenName name of the taterzen to insert in message.
     * @param taterzenNameColor color of the taterzen's name.
     * @return formatted LiteralText
     */
    public static LiteralText joinText(String message, Formatting messageColor, Text taterzenName, Formatting taterzenNameColor) {
        if(!message.contains("%s"))
            return (LiteralText) new LiteralText(message).formatted(messageColor);

        String[] split = message.split("%s");
        return (LiteralText) new LiteralText(split[0])
                .formatted(messageColor)
                .append(taterzenName.copy().formatted(taterzenNameColor))
                .append(split[1])
                .formatted(messageColor);

    }

    public static LiteralText joinString(String message, Formatting messageColor, String name, Formatting taterzenNameColor) {
        return joinText(message, messageColor, new LiteralText(name), taterzenNameColor);

    }

    public static LiteralText successText(String message, Text taterzenName) {
        return joinText(message, Formatting.GREEN, taterzenName, Formatting.YELLOW);
    }

    public static LiteralText errorText(String message, Text taterzenName) {
        return joinText(message, Formatting.RED, taterzenName, Formatting.LIGHT_PURPLE);
    }

    public static CompoundTag toTag(Text text) {
        JsonElement json = parser.parse(Text.Serializer.toJson(text));
        return (CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json);
    }

    public static Text fromTag(Tag textTag) {
        JsonElement json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, textTag);
        return Text.Serializer.fromJson(json);
    }
}
