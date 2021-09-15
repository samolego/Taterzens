package org.samo_lego.taterzens.storage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ConfigFieldList {
    public final String nodeName;
    public Object parent;
    public final List<Field> booleans;
    public final List<Field> integers;
    public final List<Field> floats;
    public final List<Field> strings;
    public final List<ConfigFieldList> nestedFields;

    public ConfigFieldList(String nodeName, Object parent, List<Field> booleans, List<Field> integers, List<Field> floats, List<Field> strings, List<ConfigFieldList> nestedFields) {
        this.floats = floats;
        this.nodeName = nodeName;
        this.parent = parent;
        this.booleans = booleans;
        this.integers = integers;
        this.strings = strings;
        this.nestedFields = nestedFields;
    }

    public static ConfigFieldList populateFields(Object parent, String nodeName) {
        Field[] attributes = parent.getClass().getFields();
        ArrayList<Field> bools = new ArrayList<>();
        ArrayList<Field> ints = new ArrayList<>();
        ArrayList<Field> floats = new ArrayList<>();
        ArrayList<Field> strings = new ArrayList<>();
        List<ConfigFieldList> nested = new ArrayList<>();

        for(Field attribute : attributes) {
            Class<?> type = attribute.getType();

            if(type.equals(boolean.class)) {
                bools.add(attribute);
            } else if(type.equals(int.class)) {
                ints.add(attribute);
            } else if(type.equals(float.class)) {
                floats.add(attribute);
            } else if(type.equals(String.class)) {
                String name = attribute.getName();
                if (!name.startsWith("_comment") && !name.equals("language"))
                    strings.add(attribute);
            } else if(!type.equals(ArrayList.class)) {
                // a subclass in our config
                try {
                    attribute.setAccessible(true);
                    Object childAttribute = attribute.get(parent);
                    nested.add(populateFields(childAttribute, attribute.getName()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return new ConfigFieldList(nodeName, parent, bools, ints, floats, strings, nested);
    }
}
