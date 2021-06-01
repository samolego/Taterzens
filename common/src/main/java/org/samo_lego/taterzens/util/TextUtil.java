package org.samo_lego.taterzens.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextUtil {
    private static final JsonParser parser = new JsonParser();

    /**
     * Inserts colored insertedText in string message.
     *
     * @param message message to insert name in.
     * @param messageColor color format of the message.
     * @param insertedText text to insert in message.
     * @param insertedTextColor color of inserted text.
     * @return formatted LiteralText
     */
    public static LiteralText joinText(String message, Formatting messageColor, Text insertedText, Formatting insertedTextColor) {
        if(!message.contains("%s"))
            return (LiteralText) new LiteralText(message).formatted(messageColor);

        String[] split = message.split("%s");
        return (LiteralText) new LiteralText(split[0])
                .formatted(messageColor)
                .append(insertedText.copy().formatted(insertedTextColor))
                .append(split.length  > 1 ? split[1] : "")
                .formatted(messageColor);

    }

    public static LiteralText joinString(String message, Formatting messageColor, String insertedString, Formatting insertedStringColor) {
        return joinText(message, messageColor, new LiteralText(insertedString), insertedStringColor);

    }

    public static LiteralText successText(String message, Text insertedText) {
        return joinText(message, Formatting.GREEN, insertedText, Formatting.YELLOW);
    }

    public static LiteralText errorText(String message, Text insertedText) {
        return joinText(message, Formatting.RED, insertedText, Formatting.LIGHT_PURPLE);
    }

    public static NbtElement toNbtElement(Text text) {
        JsonElement json = parser.parse(Text.Serializer.toJson(text));
        return (NbtCompound) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json);
    }

    public static Text fromNbtElement(NbtElement textNbtElement) {
        JsonElement json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, textNbtElement);
        return Text.Serializer.fromJson(json);
    }
}
