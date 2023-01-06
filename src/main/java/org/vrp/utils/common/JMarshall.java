package org.vrp.utils.common;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class JMarshall<T> {
    T _obj;
    private JsonParserImpl jsonParser;
    private Class<T> clazz;
    private LinkedHashMap<String, List<String>> fieldvaluemapping = new LinkedHashMap<>();
    private LinkedHashMap<String, List<String>> fieldKeymapping = new LinkedHashMap<>();
    private Map<String, String> keymap = new LinkedHashMap<>();
    private Field[] fields;
    public JMarshall(T _obj) {
        this._obj = _obj;
        System.out.println(this._obj.getClass().getName());
        populateFields();
        setClazz();
    }

    public Map<String, String> getKeyMap() {
        return keymap;
    }

    public void setKeyBinding(String fieldname, String jpath) {
        keymap.put(fieldname, jpath);
    }

    public void clearKeyBindings() {
        keymap.clear();
    }

    public void setMap(Map<String, String> map) {
        this.keymap = map;
    }

    private void setClazz() {
        clazz = (Class<T>) this._obj.getClass();
    }


    private void populateFields() {
        fields = Class.forName(_);
    }

    public void setJsonParser(JsonParserImpl jsonParserimpl) {
        this.jsonParser = jsonParserimpl;
    }

    public void loadMaps() {
        for (Field field : fields) {
            String queryfield = getFieldName(field.getName());
            List<String> queryValues = fieldvaluemapping.getOrDefault(queryfield, new LinkedList<>());
            for (Character key : keys) {
                JsonKeys _jk = _jkmap.get(key);
                ArrayList<String> matchedkeys = (ArrayList<String>) _jk.get("\\*" + queryfield).stream().filter(i -> i.contains(queryfield)).collect(Collectors.toList());
                parseField(matchedkeys);
                matchedkeys.stream().forEach(_k -> {
                    queryValues.add(jsonParser.getTemp().get(_k));
                });
            }
            fieldvaluemapping.put(queryfield, queryValues);
        }
        return null;
    }

    public void parseField(ArrayList<String> matchedKeys) {
        for () {
            for (String key : matchedKeys) {
                String[] keyparts = key.split(".");
                for (String keypart : keyparts) {
                }
            }
        }
    }
    //Idea is to get the Object mapped for disered class
    public  void parseObject(String classname) throws ClassNotFoundException {
        Field[] fields=Class.forName(classname).getDeclaredFields();
        for(Field field:fields){
            field.getType().
        }
    }
    public void parserArray(){

    }

    private String getFieldName(String fieldName) {
        String _fieldName = null;
        if (getKeyMap() != null && getKeyMap().size() > 0 && (_fieldName = getKeyMap().get(fieldName)) != null) {
            return _fieldName;
        }
        return fieldName;
    }
}
