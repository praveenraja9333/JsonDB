package org.vrp.utils.common;

import org.vrp.utils.Model1;
import org.vrp.utils.exceptions.FieldandKeyMatchException;
import org.vrp.utils.exceptions.MethodNameNotFoundException;
import org.vrp.utils.exceptions.ObjectNotSupportedException;
import org.vrp.utils.exceptions.WrongMappingException;
import org.vrp.utils.meta.FieldMap;
import org.vrp.utils.meta.Ignorable;
import org.vrp.utils.meta.RjsonArray;
import org.vrp.utils.meta.RjsonObject;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class JMarshall<T> {
    private final String ESCAPE = "\\*";
    private final String SETPREFIX="set";
    private LinkedHashMap<String, List<String>> fieldvaluemapping = new LinkedHashMap<>();
    private LinkedHashMap<String, List<String>> fieldKeymapping = new LinkedHashMap<>();
    private Map<String,List<String>> queryresultcache=new LinkedHashMap<>();
    private Map<String, String> keymap = new LinkedHashMap<>();
    private JsonParserImpl jsonParser;
    private String rootkey = "";
    private Class<T> clazz;
    private Field[] fields;
    private T _obj;
    private Stack<Method> method=new Stack<>();
    private Stack<String> rks=new Stack<>();
    private boolean ignorablecurser=false;


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

        public Class<?> getClazz() {
            return clazzname;
        }

        public static DA_TYPES getFromClazz(Class clazzname) {
            for (DA_TYPES da_types : DA_TYPES.values()) {
                if (da_types.getClazz() == clazzname) {
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
        LinkedList<String> list=(LinkedList)getIntialFieldKey(initialField);
        while(!list.isEmpty()){
            rks.clear();
            String fieldname=list.pollFirst();
            rootkey=fieldname.substring(0,fieldname.indexOf(initialField.getName()));
            rks.push(rootkey);
            System.out.println(fieldname);
            //parseObject(fieldname);
        }
    }
    public String getValue(String finalkey){
        return jsonParser.getTemp().get(finalkey);
    }
    private Map<String,Map<Field,Class<?>>> getFieldMapping(Class<?> clazz){
         Field[] fields=clazz.getDeclaredFields();
        Map<String,Map<Field,Class<?>>> map = new LinkedHashMap<>();
         for(Field field:fields) {
             Class<?> fieldclazz = field.getType();
             Annotation anno;
             anno=field.getAnnotation(RjsonObject.class)==null?field.getAnnotation(RjsonArray.class):field.getAnnotation(RjsonObject.class);
             anno=anno==null?field.getAnnotation(Ignorable.class):anno;
             if (anno != null) {
                 String objtype=anno.toString().substring(anno.toString().lastIndexOf(".")+1).replace("()","");
                 Map<Field,Class<?>> tempmap=map.getOrDefault(objtype,new LinkedHashMap<>());
                 tempmap.put(field,fieldclazz);
                 map.put(objtype,tempmap);
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

    public List<String> getIntialFieldKey(Field field){
        Map<Character,JsonKeys> map=this.jsonParser.getKeydatastore();
        List<String> list=new LinkedList<>();
        for(Character ch:map.keySet()){
            JsonKeys jk=map.get(ch);
            list.addAll(jk.get("\\*"+field.getName()));
        }
        return list;
    }
    public String getFieldKey(String field){
        Map<Character,JsonKeys> map=this.jsonParser.getKeydatastore();
        String fieldname=rks.peek()+"\\*"+field;
        List<String> list=new LinkedList<>();
        JsonKeys jk=map.get(fieldname.charAt(0));
        list.addAll(jk.get(fieldname));

        return list.stream().collect(Collectors.joining(","));
    }
    private boolean isObject(String key,Field field){
        validateFieldKeyMatch(key,field.getName());
        return key.charAt(getFieldKeyIndex(key,field.getName()))=='.'?true:false;
    }
    private boolean isArray(String key,Field field){
        validateFieldKeyMatch(key,field.getName());
        return key.charAt(getFieldKeyIndex(key,field.getName()))=='['?true:false;
    }
    private int getFieldKeyIndex(String key,Field field){
        validateFieldKeyMatch(key,rootkey);
        int rkl=rootkey.length();
        return rkl+key.substring(rkl).indexOf(field.getName())+field.getName().length();
    }
    private void validateFieldKeyMatch(String key,String field){
        if(!key.contains(field)){
            throw new FieldandKeyMatchException(key+" and "+field+" does not match");
        }
    }
    public int getFieldKeyIndex(String key,String field){
        validateFieldKeyMatch(key,field);
        int rkl=rootkey.length();
        return rkl+key.substring(rkl).indexOf(field)+field.length();
    }
    private<N> void insertObject(Method method, N childpojo ){
            method
    }
    private String getRootkey(Field field){
        String fetchedkey=getFieldKey(field.getName());
        String returnkey= fetchedkey.substring(0,fetchedkey.indexOf(field.getName()));
        return returnkey!=null?returnkey:null;
    }
    private Method getMethod(Field field,Class<?> clazz){
        Method method=Arrays.asList(clazz.getDeclaredMethods()).stream().filter(m->m.getName().toLowerCase().contains(SETPREFIX+field.getName().toLowerCase())).findFirst().orElse(null);
        if (method==null){
            Annotation anno=field.getAnnotation(FieldMap.class);
            FieldMap fm=anno!=null?(FieldMap)anno:null;
            String mappedMethod=fm.fieldname();
            method=Arrays.asList(clazz.getClass().getDeclaredMethods()).stream().filter(m->m.getName().toLowerCase().contains(mappedMethod.toLowerCase())).findFirst().orElse(null);
            return method;
        }
        return null;
    }
    private<P> Object parsePremitive(P pojo,Field field) {
            String finalkey=getFieldKey(field.getName());
            String key=rks.peek();
            if(isObject(key,field)||isArray(key,field)||!ignorablecurser){
                throw new WrongMappingException(key+"Is not primitive type. Check mapping or Configure the field nullable");
            }
            Method method=getMethod(field,pojo.getClass());
            if(method==null){
                throw new MethodNameNotFoundException("Assocatied method for field was not, Please check and set Proper POJO with standard convention or use FieldMap annotation for custom methodName");
            }
            method.invoke(pojo);
            return null;
    }

    //Idea is to get the Object mapped for disered class
    public<P> P parseObject(Class<P> pojo) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Field[] fields=pojo.getClass().getDeclaredFields();
        P obj=pojo.getDeclaredConstructor().newInstance();
        rootkey=rks.peek();
        Map<String,Map<Field,Class<?>>> map=getFieldMapping(pojo.getClass());
        Method[] methods=pojo.getDeclaredMethods();
        META_ENUM meta;
        for(Field field:fields){
            ignorablecurser=false;
            META_ENUM meta_enum=null;
            for(String Robjects:map.keySet()){
                if(map.get(Robjects).containsKey(field)){
                    meta_enum=META_ENUM.getEnum(Robjects);
                };
            }
            switch (meta_enum) {
                case RJSONARRAY:
                case RJSONOBJECT:
                    String tempkey=getRootkey(field);
                    validateRookKey(tempkey,true);
                    rks.push(rootkey);
                    Method method=getMethod(field,pojo);
                    method.invoke(obj,parseObject(field.getType()));
                    break;
                case RJSONROUTE:
                case IGNORABLE:
                case RNULLABLE:
                    ignorablecurser=true;
                default:
                    parsePremitive(obj,field);
                    break;
            }
            //String key=getFieldName(ESCAPE+Objname+ESCAPE+field.toString());
            }
        rks.pop();
        return obj;
    }

    private boolean validateRookKey(String tempkey,boolean errorflag) {
        if(tempkey==null&&errorflag){
            throw new FieldandKeyMatchException(rootkey+" Is not valid key. Please check");
        }
        return tempkey==null;
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
        /*JsonParserImpl jsonParser = new JsonParserImpl();
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
        System.out.println(Key.substring(0,Key.lastIndexOf(field)+field.length()));*/
        Method[] methods=Model1.class.getDeclaredMethods();
        Method method=Arrays.asList(methods).stream().filter(s->s.getName().toLowerCase().contains("getsiteId".toLowerCase())).findFirst().orElse(null);

            System.out.println("Methods "+method);

    }
}
