package org.vrp.utils.common;

import org.vrp.utils.Model1;
import org.vrp.utils.exceptions.FieldandKeyMatchException;
import org.vrp.utils.exceptions.ObjectNotSupportedException;
import org.vrp.utils.meta.Ignorable;
import org.vrp.utils.meta.RjsonObject;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class JMarshall<T> {
    private final String ESCAPE="\\*";
    T _obj;
    private JsonParserImpl jsonParser;
    private Class<T> clazz;
    private LinkedHashMap<String, List<String>> fieldvaluemapping = new LinkedHashMap<>();
    private LinkedHashMap<String, List<String>> fieldKeymapping = new LinkedHashMap<>();
    private Map<String, String> keymap = new LinkedHashMap<>();
    private Field[] fields;

    private String rootkey="";

    enum DA_TYPES {
        STRING(java.lang.String.class),
        DOUBLE(java.lang.Double.class),
        INTEGER(java.lang.Integer.class),
        FLOAT(java.lang.Float.class),
        LONG(java.lang.Long.class),
        DEFAULT(Object.class);
        private Class clazzname;

        DA_TYPES(Class clazzname) {
            this.clazzname = clazzname;
        }
        @Override
        public String toString() {
            return clazzname.getName();
        }
        public Class<?> getClazz(){
            return clazzname;
        }
        public static DA_TYPES getFromClazz(Class clazzname){
            for(DA_TYPES da_types:DA_TYPES.values()){
                if(da_types.getClazz()==clazzname){
                    return da_types;
                }
            }
            return DEFAULT;
        }
    }
    public JMarshall(){

    }
    public JMarshall(T _obj) {
        this._obj = _obj;
        System.out.println(this._obj.getClass().getName());
        //parser(this._obj.getClass());
        populateFields();
        //setClazz();
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
        //fields = Class.forName();
    }
    public void setJsonParser(JsonParserImpl jsonParserimpl) {
        this.jsonParser = jsonParserimpl;
    }
    public void parser(Class classname) throws ClassNotFoundException {
        Field initialField=classname.getDeclaredFields()[0];
        LinkedList<String> list=(LinkedList)getIntialField(initialField);
        while(!list.isEmpty()){
            String fieldname=list.pollFirst();
            System.out.println(fieldname);
            //parseObject(fieldname);
        }
    }
    private Map<String,Map<Field,Class<?>>> getFieldMapping(Class<?> clazz){
         Field[] fields=clazz.getDeclaredFields();
        Map<String,Map<Field,Class<?>>> map = new LinkedHashMap<>();
         for(Field field:fields) {
             Class<?> fieldclazz = field.getType();
             getIntialField(field);
             Annotation anno;
             anno=field.getAnnotation(RjsonObject.class)==null?field.getAnnotation(RjsonObject.class):field.getAnnotation(RjsonObject.class);
             anno=anno==null?field.getAnnotation(Ignorable.class):anno;
             if (anno != null) {
                 Map<Field,Class<?>> tempmap=map.getOrDefault(anno.toString(),new LinkedHashMap<>());
                 tempmap.put(field,fieldclazz);
                 map.put(anno.toString(),tempmap);
                 continue;
             }
             if(!isValidField(field)){
                 throw new ObjectNotSupportedException("Wrong fields, Please annonate the custom objects");
             }
         }
         return map;
    }

    private boolean isValidField(Field field) {
        Class<?> clazz=field.getType();
        DA_TYPES da_type=DA_TYPES.getFromClazz(clazz);
        switch (da_type) {
            case STRING:
            case INTEGER:
            case FLOAT:
            case LONG:
            case DOUBLE:
                return true;
            default:
                return false;
        }
    }

    public List<String> getIntialField(Field field){
        Map<Character,JsonKeys> map=this.jsonParser.getKeydatastore();
        List<String> list=new LinkedList<>();
        for(Character ch:map.keySet()){
            JsonKeys jk=map.get(ch);
            list.addAll(jk.get("\\*"+field.getName()));
        }
        return list;
    }
    public List<String> getField(String field){
        Map<Character,JsonKeys> map=this.jsonParser.getKeydatastore();
        List<String> list=new LinkedList<>();
        JsonKeys jk=map.get(field.charAt(0));
        list.addAll(jk.get("\\*"+field));

        return list;
    }
    private boolean isObject(String key,Field field){
        validateFieldMatch(key,field);
        return key.charAt(getFieldIndex(key,field))=='.'?true:false;
    }
    private boolean isArray(String key,Field field){
        validateFieldMatch(key,field.getName());
        return key.charAt(getFieldIndex(key,field))=='['?true:false;
    }
    private int getFieldIndex(String key){
        validateFieldMatch(key,rootkey);
        int rkl=rootkey.length();
        return rkl+key.substring(rkl).indexOf(field.getName())+field.getName().length();
    }
    private void validateFieldMatch(String key,String field){
        int fl=field.length();
        int kl=key.length();
        if(!key.contains(field.getName())){
            throw new FieldandKeyMatchException(key+" and "+field.getName()+" does not match");
        }
    }

    //Testing
    public int getFieldIndex(String key,String field){
        int rkl=rootkey.length();
        if(rootkey.equals("")||!key.contains(rootkey)){
            new FieldandKeyMatchException(key+" and "+field.length()+" does not match");
        }
        return rkl+key.substring(rkl).indexOf(field)+field.length();
    }

    //Idea is to get the Object mapped for disered class
    public  void parseObject(String classname,Field Objname) throws ClassNotFoundException {
        Class clazz=Class.forName(classname);
        Field[] fields=clazz.getDeclaredFields();
        Map<String,Map<Field,Class<?>>> map=getFieldMapping(clazz);
        boolean firstflag=true;
        for(Field field:fields){
            if(firstflag){
                firstflag=true;
            }
            String key=getFieldName(ESCAPE+Objname+ESCAPE+field.toString());
            //if(IsKeyObject(Key,field.name)){

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
    public static void main(String[] args) throws ClassNotFoundException {
        JsonParserImpl jsonParser = new JsonParserImpl();
        jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\Array.json"));
        jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\hotels_response_API.json"));
        jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\gitsamplebigjson.json"));
        LinkedHashMap<Character, JsonKeys> l = jsonParser.getKeydatastore();
        JMarshall jMarshall=new JMarshall(new Model1());
        jMarshall.setJsonParser(jsonParser);
        jMarshall.parser(Model1.class);
        Map map=jMarshall.getFieldMapping(Model1.class);
        String Key="Hello.Hi.Bye";
        String field="Hi";
        int a=jMarshall.getFieldIndex(Key,field);
        System.out.println(Key.substring(0,Key.lastIndexOf(field)+field.length()));
    }
}
