package org.samo_lego.taterzens.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.Arrays;

import static org.samo_lego.taterzens.Taterzens.lang;
import static org.samo_lego.taterzens.compatibility.ModDiscovery.SERVER_TRANSLATIONS_LOADED;

public class TextUtil {

    /**
     * Inserts colored insertedText in string message.
     */
    public static MutableComponent joinText(String key, ChatFormatting messageColor, ChatFormatting insertedTextColor, String... insertedString) {
        Object[] texts = Arrays.stream(insertedString).map(s -> Component.literal(s).withStyle(insertedTextColor)).toArray();
        return translate(key, texts).plainCopy().withStyle(messageColor);
    }

    public static MutableComponent successText(String key, String... insertedText) {
        return joinText(key, ChatFormatting.GREEN, ChatFormatting.YELLOW, insertedText);
    }

    public static MutableComponent errorText(String key, String... insertedText) {
        return joinText(key, ChatFormatting.RED, ChatFormatting.LIGHT_PURPLE, insertedText);
    }

    /**
     * Converts {@link Component} to {@link Tag}.
     * @param text text to convert.
     * @return NbtElement generated from text.
     */
    public static Tag toNbtElement(Component text) {
        JsonElement json = JsonParser.parseString(Component.Serializer.toJson(text));
        return JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json);
    }

    /**
     * Creates a {@link MutableComponent} from {@link Tag}.
     * @param textNbtElement text nbt to convert to text
     * @return mutable text object..
     */
    public static MutableComponent fromNbtElement(Tag textNbtElement) {
        JsonElement json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, textNbtElement);
        return Component.Serializer.fromJson(json);
    }

    /**
     * Gets the text for the given language key.
     *
     * @param key lang key.
     * @return {@link TranslatableContents} or {@link LiteralContents} depending on whether SERVER_TRANSLATIONS is loaded.
     */
    public static MutableComponent translate(String key, Object... args) {
        if(SERVER_TRANSLATIONS_LOADED) {
            return Component.translatable(key, args);
        }

        String translation;
        if(lang.has(key))
            translation = lang.get(key).getAsString();
        else
            translation = key;
        return Component.translatable(translation, args);
    }

}
