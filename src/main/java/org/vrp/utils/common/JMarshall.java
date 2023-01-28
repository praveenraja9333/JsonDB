package org.vrp.utils.common;

import org.vrp.utils.Models.GitUserlogin;
import org.vrp.utils.Models.Model1;
import org.vrp.utils.exceptions.FieldandKeyMatchException;
import org.vrp.utils.exceptions.MethodNameNotFoundException;
import org.vrp.utils.exceptions.ObjectNotSupportedException;
import org.vrp.utils.exceptions.WrongMappingException;
import org.vrp.utils.meta.*;

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

    private ArrayList<T> arrtemp=new ArrayList<>();

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
    public void parser(Class classname) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Field initialField=getInitialField(classname);
        Map<String,String> fieldMap=getFieldMapping(classname);
        LinkedList<String> list=(LinkedList)getIntialFieldKey(initialField) ;
        if(list.isEmpty())return;
        String parentString=list.get(0);
        list=new LinkedList<>(list.stream().filter(s->s.split("\\.").length==parentString.split("\\.").length).sorted().collect(Collectors.toList()));
        while(!list.isEmpty()){
            rks.clear();
            String fieldname=list.pollFirst();
            rootkey=fieldname.substring(0,fieldname.indexOf(initialField.getName()));
            rks.push(rootkey);
            System.out.println(fieldname);
            T m=(T)parseObject(classname);
            arrtemp.add(m);
           // System.out.println(m.getSiteId()+"            "+m.getCountryCode());
        }
    }

    private List<String> getSortedList(){
        return new LinkedList<>();
    }
    private Field getInitialField(Class classname) {
        Field[] fields=classname.getDeclaredFields();
        Field InitailField=fields[0];
        LinkedHashMap<String,String> fmap=new LinkedHashMap<>();
        for(Field field:fields) {
            Class<?> fieldclazz = field.getType();
            Annotation anno;
            anno=field.getAnnotation(RootElement.class)==null?null:field.getAnnotation(RootElement.class);
            if (anno != null) {
                return field;
            }
        }
        return InitailField;
    }

    public String getValue(String finalkey){
        return jsonParser.getTemp().get(finalkey);
    }
    private Map<String,Map<Field,Class<?>>> getFieldObjectMapping(Class<?> clazz){
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
    private Map<String,String> getFieldMapping(Class<?> clazz){
        Field[] fields=clazz.getDeclaredFields();
        LinkedHashMap<String,String> fmap=new LinkedHashMap<>();
        for(Field field:fields) {
            Class<?> fieldclazz = field.getType();
            Annotation anno;
            anno=field.getAnnotation(FieldMap.class)==null?null:field.getAnnotation(FieldMap.class);
            if (anno != null) {
                anno=(FieldMap)anno;
                String fmstr=((FieldMap) anno).fieldname();
                fmap.put(field.getName(),fmstr);
            }
        }
        return fmap;
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
    public String getExactFieldKey(String field){
        Map<Character,JsonKeys> map=this.jsonParser.getKeydatastore();
        String fieldname=rks.peek()+field;
        List<String> list=new LinkedList<>();
        JsonKeys jk=map.get(fieldname.charAt(0));
        list.addAll(jk.get(fieldname));

        return list.stream().collect(Collectors.joining(","));
    }
    private boolean isObject(String key,String fieldname){
        validateFieldKeyMatch(key,fieldname);
        if(getFieldKeyIndex(key,fieldname)<key.length())
        return key.charAt(getFieldKeyIndex(key,fieldname))=='.'?true:false;
    return false;
    }
    private boolean isArray(String key,String fieldname){
        validateFieldKeyMatch(key,fieldname);
        if(getFieldKeyIndex(key,fieldname)<key.length())
        return key.charAt(getFieldKeyIndex(key,fieldname))=='['?true:false;
        return false;
    }
    private int getFieldKeyIndex(String key,Field field){
        validateFieldKeyMatch(key,rootkey);
        int rkl=rootkey.length();

        return rkl+key.substring(rkl).indexOf(field.getName())+field.getName().length()-1;
    }
    private void validateFieldKeyMatch(String key,String field){
        if(!key.toLowerCase().contains(field.toLowerCase())){
            throw new FieldandKeyMatchException(key+" and "+field+" does not match");
        }
    }
    public int getFieldKeyIndex(String key,String field){
        validateFieldKeyMatch(key,field);
        int rkl=rootkey.length();
        return rkl+key.substring(rkl).indexOf(field)+field.length();
    }
    private<N> void insertObject(Method method, N childpojo ){
            //method
    }
    private String getRootkey(String fieldname){
        String fetchedkey=getFieldKey(fieldname);
        String returnkey= fetchedkey.substring(0,fetchedkey.indexOf(fieldname));
        return returnkey!=null?returnkey:null;
    }
    private Method getMethod(Field field,Class<?> clazz){
        Method method=Arrays.asList(clazz.getDeclaredMethods()).stream().filter(m->m.getName().toLowerCase().contains(SETPREFIX+field.getName().toLowerCase())).findFirst().orElse(null);
        if (method==null){
            Annotation anno=field.getAnnotation(MethodMap.class);
            MethodMap mM=anno!=null?(MethodMap)anno:null;
            String mappedMethod=mM.methodName();
            method=Arrays.asList(clazz.getClass().getDeclaredMethods()).stream().filter(m->m.getName().toLowerCase().contains(mappedMethod.toLowerCase())).findFirst().orElse(null);
            return method;
        }
        return method;
    }
    private<P> Object parsePremitive(P pojo,Field field,String fieldname) throws InvocationTargetException, IllegalAccessException {
            String finalkey=getExactFieldKey(fieldname);
            String key=rks.peek();
            if(isObject(finalkey,fieldname)||isArray(finalkey,fieldname)&&!ignorablecurser){
                throw new WrongMappingException(key+"Is not primitive type. Check mapping or Configure the field nullable");
            }
            Method method=getMethod(field,pojo.getClass());
            if(method==null){
                throw new MethodNameNotFoundException("Assocatied method for field was not, Please check and set Proper POJO with standard convention or use FieldMap annotation for custom methodName");
            }
            String value=jsonParser.getTemp().get(finalkey);
            method.invoke(pojo,value);
            return pojo;
    }

    //Idea is to get the Object mapped for disered class
    public<P> P parseObject(Class<P> pojo) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Field[] fields=pojo.getDeclaredFields();
        P obj=pojo.getDeclaredConstructor().newInstance();
        rootkey=rks.peek();
        Map<String,Map<Field,Class<?>>> map=getFieldObjectMapping(pojo);
        Map<String,String> fieldMap=getFieldMapping(pojo);
        Method[] methods=pojo.getDeclaredMethods();
        META_ENUM meta;
        for(Field field:fields){
            String fname=fieldMap.get(field.getName());
            fname=fname==null?field.getName():fname;
            ignorablecurser=false;
            META_ENUM meta_enum=null;
            for(String Robjects:map.keySet()){
                if(map.get(Robjects).containsKey(field)){
                    meta_enum=META_ENUM.getEnum(Robjects);
                };
            }
            if(meta_enum==null)meta_enum=META_ENUM.PRIMITIVES;
            switch (meta_enum) {
                case RJSONARRAY:
                case RJSONOBJECT:
                    String tempkey=getRootkey(fname);
                    validateRookKey(tempkey,true);
                    rks.push(rootkey+fname+".");
                    Method method=getMethod(field,pojo);
                    method.invoke(obj,parseObject(field.getType()));
                    break;
                case RJSONROUTE:
                case IGNORABLE:
                case RNULLABLE:
                    ignorablecurser=true;
                default:
                    parsePremitive(obj,field,fname);
                    break;
            }
            //String key=getFieldName(ESCAPE+Objname+ESCAPE+field.toString());
            }
        rks.pop();
        rootkey=rks.isEmpty()?"":rks.peek();
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
    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        JsonParserImpl jsonParser = new JsonParserImpl();
        jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\Array.json"));
        //jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\hotels_response_API.json"));
       // jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\gitsamplebigjson.json"));
        LinkedHashMap<Character, JsonKeys> l = jsonParser.getKeydatastore();
        JMarshall<Model1> jMarshall=new JMarshall();
        jMarshall.setJsonParser(jsonParser);
        jMarshall.parser(Model1.class);
        /*ArrayList a=jMarshall.arrtemp;
        Map map=jMarshall.getFieldMapping(Model1.class);
        String Key="Hello.Hi.Bye";
        String field="Hi";
        //int a=jMarshall.getFieldIndex(Key,field);
        System.out.println(Key.substring(0,Key.lastIndexOf(field)+field.length()));
        Method[] methods=Model1.class.getDeclaredMethods();
        Method method=Arrays.asList(methods).stream().filter(s->s.getName().toLowerCase().contains("getsiteId".toLowerCase())).findFirst().orElse(null);
            System.out.println("Methods "+method);  */
    }
}
