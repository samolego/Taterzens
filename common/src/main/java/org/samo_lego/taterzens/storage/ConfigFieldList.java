package org.samo_lego.taterzens.storage;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.samo_lego.taterzens.storage.TaterConfig.COMMENT_PREFIX;

/**
 * Creates an object containing lists with primitives, {@link String}s and nested {@link ConfigFieldList}s.
 */
public record ConfigFieldList(Field parentField, Object parent, List<Field> booleans, List<Field> integers, List<Field> floats, List<Field> strings, List<ConfigFieldList> nestedFields) {

    /**
     * Generates a {@link ConfigFieldList} for selected object with recursion.
     * Supports nested values as well.
     *
     * @param parentField - field whose name will be used for the command node. If null, it will default to "edit",
     *                    as the only config object that doesn't have a field is object itself, as it's a class.
     * @param parent - object to generate {@link ConfigFieldList} for
     */
    public static ConfigFieldList populateFields(@Nullable Field parentField, Object parent) {
        ArrayList<Field> bools = new ArrayList<>();
        ArrayList<Field> ints = new ArrayList<>();
        ArrayList<Field> floats = new ArrayList<>();
        ArrayList<Field> strings = new ArrayList<>();
        List<ConfigFieldList> nested = new ArrayList<>();

        for(Field attribute : parent.getClass().getFields()) {
            Class<?> type = attribute.getType();

            if(type.equals(boolean.class)) {
                bools.add(attribute);
            } else if(type.equals(int.class)) {
                ints.add(attribute);
            } else if(type.equals(float.class)) {
                floats.add(attribute);
            } else if(type.equals(String.class)) {
                String name = attribute.getName();
                if(!name.startsWith(COMMENT_PREFIX) && !name.equals("language"))
                    strings.add(attribute);
            } else if(!type.equals(ArrayList.class)) {
                // a subclass in our config
                try {
                    attribute.setAccessible(true);
                    Object childAttribute = attribute.get(parent);
                    nested.add(populateFields(attribute, childAttribute));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return new ConfigFieldList(
            parentField,
            parent,
            Collections.unmodifiableList(bools),
            Collections.unmodifiableList(ints),
            Collections.unmodifiableList(floats),
            Collections.unmodifiableList(strings),
            Collections.unmodifiableList(nested)
        );
    }

}
