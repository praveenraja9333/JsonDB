package org.vrp.utils.common;

import org.vrp.utils.Models.*;
import org.vrp.utils.exceptions.*;
import org.vrp.utils.meta.*;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class JMarshall<T>{
    private final String ESCAPE = "\\*";
    private final String SETPREFIX = "set";
    private LinkedHashMap<String, List<String>> fieldvaluemapping = new LinkedHashMap<>();
    private LinkedHashMap<String, List<String>> fieldKeymapping = new LinkedHashMap<>();
    private Map<String, List<String>> queryresultcache = new LinkedHashMap<>();
    private Map<String, String> keymap = new LinkedHashMap<>();
    private JsonParserImpl jsonParser;
    private String rootkey = "";
    private Class<T> clazz;
    private Field[] fields;
    private T _obj;
    private Stack<Method> method = new Stack<>();
    private Stack<String> rks = new Stack<>();
    private boolean ignorablecurser = false;

    private ArrayList<T> arrtemp = new ArrayList<>();

    public ArrayList<T> getArrtemp() {
        return arrtemp;
    }

    enum DA_TYPES {
        STRING(java.lang.String.class), DOUBLE(java.lang.Double.class), INTEGER(java.lang.Integer.class), FLOAT(java.lang.Float.class), LONG(java.lang.Long.class), DEFAULT(Object.class);
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
            if (clazzname.getName().equals("int")) {
                return INTEGER;
            } else if (clazzname.getName().equals("double")) {
                return DOUBLE;
            } else if (clazzname.getName().equals("float")) {
                return FLOAT;
            }

            return DEFAULT;
        }
    }

    public JMarshall() {

    }

    public void setJsonParser(JsonParserImpl jsonParserimpl) {
        this.jsonParser = jsonParserimpl;
    }

    public void parser(Class classname) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String initialField = getInitialField(classname);
        boolean isInitialArray=isInitalArray(classname);
        int indexIndex=0;
        int counter=0;
        String parentString="";
        LinkedList<String> list =null;
        if(!"".equals(initialField)) {
            list = (LinkedList) getIntialFieldKey(initialField);
            if (list.isEmpty()) return;
            parentString = list.get(0);
            counter = 0;
            indexIndex = parentString.indexOf(initialField) + initialField.length();
        }
        if(isInitialArray){
            rks.clear();
            rootkey="";
            rks.push("");
            arrtemp.add((T)parseObject(classname));
            return;
        }else {
            final int parentlength = parentString.split("\\.").length;
            list = new LinkedList<>(list.stream().filter(s -> s.split("\\.").length == parentlength).sorted().collect(Collectors.toList()));
        }
        while (!list.isEmpty()) {
            rks.clear();
            String fieldName = list.pollFirst();
            rootkey = fieldName.substring(0, fieldName.indexOf(initialField));
            rks.push(rootkey);
            System.out.println(fieldName);
            System.out.println("FieldName " + fieldName);
            T m = (T) parseObject(classname);
            arrtemp.add(m);
        }
    }



    //Idea is to get the Object mapped for disered class
    public <P> P parseObject(Class<P> pojo) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Field[] fields = pojo.getDeclaredFields();
        P obj = pojo.getDeclaredConstructor().newInstance();
        rootkey = rks.peek();
        Map<String, Map<Field, Class<?>>> map = getFieldObjectMapping(pojo);
        Map<String, String> fieldMap = getFieldMapping(pojo);
        Method[] methods = pojo.getDeclaredMethods();
        META_ENUM meta;

        for (Field field : fields) {
            String fName = fieldMap.get(field.getName());
            fName = fName == null ? field.getName() : fName;
            ignorablecurser = false;
            String tempKey;
            Method method;
            META_ENUM meta_enum = null;
            for (String Robjects : map.keySet()) {
                if (!Robjects.equals("Rnullable") && map.get(Robjects).containsKey(field)) {
                    meta_enum = META_ENUM.getEnum(Robjects);
                }
                ;
            }
            if (meta_enum == null) meta_enum = META_ENUM.PRIMITIVES;
            switch (meta_enum) {
                case RJSONARRAY:
                   /* tempKey = getRootkey(fName);
                    if (tempKey==null&&!isInitalArray(pojo.getClass())) {
                        Map<Field, Class<?>> mapset = map.get("Rnullable");
                        if (mapset != null && mapset.get(field) != null) break;
                        else {
                            throw new MandatoryMemberMissingException(field.getName() + " field missing, Consider making nullable or check with source");
                        }
                    } */
                    rks.push(rootkey + fName);
                    method = getMethod(field, pojo);
                    method.invoke(obj, parseArray(field, obj));
                    break;
                case RJSONOBJECT:
                    tempKey = getRootkey(fName);
                    if (tempKey==null) {
                        Map<Field, Class<?>> mapset = map.get("Rnullable");
                        if (mapset != null && mapset.get(field) != null) break;
                        else {
                            throw new MandatoryMemberMissingException(field.getName() + " field missing, Consider making nullable or check with source");
                        }
                    }
                    rks.push(rootkey + fName + ".");
                    method = getMethod(field, pojo);
                    method.invoke(obj, parseObject(field.getType()));
                    break;
                case RJSONROUTE:
                case IGNORABLE:
                case RNULLABLE:
                    ignorablecurser = true;
                default:
                    parsePremitive(obj, field, fName);
                    break;
            }
        }
        rks.pop();
        rootkey = rks.isEmpty() ? "" : rks.peek();
        return obj;
    }

    private <P> List<Object> parseArray(Field field, P pojo) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        RjsonArray rar = (RjsonArray) field.getAnnotation(RjsonArray.class);
        boolean premitiveflag = false;
        LinkedList<Object> returnlist;
        Class<?> clazz;
        try {
            String type = rar.type();
            clazz = Class.forName(rar.type());
            returnlist = new LinkedList<>();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (isPremitive(clazz)) {
            premitiveflag = true;
        }
        int counter = 0;
        if(!premitiveflag){
            while(!getExactFieldKey("[" + counter + "]").equals("")) {
                rootkey = rks.peek() + "[" + counter++ + "].";
                rks.push(rootkey);
                returnlist.add(parseObject(clazz));
                rootkey=rks.peek();
            }
            rks.pop();
            rootkey = rks.isEmpty() ? "" : rks.peek();
            return returnlist;
        }
        while (true) {
            String finalKey = getExactFieldKey("[" + counter++ + "]");
            String value = jsonParser.getTemp().get(finalKey);
            String key = rks.peek();

            if (value != null && "".equals(value) && isObject(finalKey, field.getName()) && !ignorablecurser) {
                throw new WrongMappingException(key + "Is not primitive type. Check mapping or Configure the field nullable");
            }
            System.out.println("Hello");
            if (value != null && !value.equals("")) {
                returnlist.add(value);
            } else {
                break;
            }
        }
        rks.pop();
        rootkey = rks.isEmpty() ? "" : rks.peek();
        return returnlist;
    }


    private <P> Object parsePremitive(P pojo, Field field, String fieldName) throws InvocationTargetException, IllegalAccessException {
        String finalKey = getExactFieldKey(fieldName);
        finalKey=finalKey.equals("")?getFieldKey(fieldName):finalKey;
        String key = rks.peek();
        if ((isObject(finalKey, fieldName) || isArray(finalKey, fieldName)) && !ignorablecurser) {
            throw new WrongMappingException(key + fieldName + " Is not primitive type. Check mapping or Configure the field nullable");
        }
        Method method = getMethod(field, pojo.getClass());
        if (method == null) {
            throw new MethodNameNotFoundException("Assocatied method for field was not, Please check and set Proper POJO with standard convention or use FieldMap annotation for custom methodName");
        }
        String value = jsonParser.getTemp().get(finalKey);
        method.invoke(pojo, parseValue(field, value));
        return pojo;
    }

    private Object parseValue(Field field, String value) {
        Class<?> clazz = field.getType();
        DA_TYPES da_type = DA_TYPES.getFromClazz(clazz);
        switch (da_type) {
            case INTEGER:
                return Integer.parseInt(value);
            case FLOAT:
                return Float.parseFloat(value);
            case LONG:
                return Long.parseLong(value);
            case DOUBLE:
                return Double.parseDouble(value);
            default:
                return value;
        }
    }

    private boolean validateRookKey(String tempKey, boolean errorFlag) {
        if (tempKey == null && errorFlag) {
            throw new FieldandKeyMatchException(rootkey + " Is not valid key. Please check");
        }
        return tempKey == null;
    }


    private Method getMethod(Field field, Class<?> clazz) {
        Method method = Arrays.asList(clazz.getDeclaredMethods()).stream().filter(m -> m.getName().toLowerCase().contains(SETPREFIX + field.getName().toLowerCase())).findFirst().orElse(null);
        if (method == null) {
            Annotation anno = field.getAnnotation(MethodMap.class);
            MethodMap mM = anno != null ? (MethodMap) anno : null;
            String mappedMethod = mM.methodName();
            method = Arrays.asList(clazz.getClass().getDeclaredMethods()).stream().filter(m -> m.getName().toLowerCase().contains(mappedMethod.toLowerCase())).findFirst().orElse(null);
            return method;
        }
        return method;
    }

    public int getFieldKeyIndex(String key, String field) {
        validateFieldKeyMatch(key, field);
        int rkl = rootkey.length();
        return rkl + key.substring(rkl).indexOf(field) + field.length();
    }


    private String getRootkey(String fieldName) {
        String fetchedKey = getFieldKey(fieldName);
        if ("".equals(fetchedKey)) return null;
        String returnKey = fetchedKey.substring(0, fetchedKey.indexOf(fieldName));
        return returnKey != null ? returnKey : null;
    }

    private int getFieldKeyIndex(String key, Field field) {
        validateFieldKeyMatch(key, rootkey);
        int rkl = rootkey.length();

        return rkl + key.substring(rkl).indexOf(field.getName()) + field.getName().length() - 1;
    }


    public List<String> getIntialFieldKey(String fieldName) {
        Map<Character, JsonKeys> map = this.jsonParser.getKeyDataStore();
        List<String> list = new LinkedList<>();
        for (Character ch : map.keySet()) {
            JsonKeys jk = map.get(ch);
            list.addAll(jk.get("\\*" + fieldName));
        }
        return list;
    }

    public String getFieldKey(String field) {
        if(field==null||"".equals(field))return "";
        Map<Character, JsonKeys> map = this.jsonParser.getKeyDataStore();
        String fieldname = rks.peek().equals("")?field:rks.peek()+ "\\*" + field;
        List<String> list = new LinkedList<>();
        JsonKeys jk = map.get(fieldname.charAt(0));
        list.addAll(jk.get(fieldname));
        if (list.isEmpty()) return "";
        return list.get(0);
    }

    public String getExactFieldKey(String field) {
        Map<Character, JsonKeys> map = this.jsonParser.getKeyDataStore();
        String fieldName = rks.peek() + field;
        List<String> list = new LinkedList<>();
        JsonKeys jk = map.get(fieldName.charAt(0));
        if (jk==null) return "";
        list.addAll(jk.get(fieldName));
        if (list.isEmpty()) return "";
        return list.stream().collect(Collectors.joining(","));
    }

    private Map<String, Map<Field, Class<?>>> getFieldObjectMapping(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        Map<String, Map<Field, Class<?>>> map = new LinkedHashMap<>();
        for (Field field : fields) {
            Class<?> fieldClazz = field.getType();
            Annotation anno;
            anno = field.getAnnotation(RjsonObject.class) == null ? field.getAnnotation(RjsonArray.class) : field.getAnnotation(RjsonObject.class);
            anno = anno == null ? field.getAnnotation(Ignorable.class) : anno;
            if (anno != null) {
                String objType = anno.toString().replaceAll("\\(.*\\)", "");//
                objType = objType.substring(objType.lastIndexOf(".") + 1);
                Map<Field, Class<?>> tempMap = map.getOrDefault(objType, new LinkedHashMap<>());
                tempMap.put(field, fieldClazz);
                map.put(objType, tempMap);
                anno = field.getAnnotation(Rnullable.class);
                if (anno != null) {
                    objType = anno.toString().replaceAll("\\(.*\\)", "");
                    objType = objType.substring(objType.lastIndexOf(".") + 1);
                    tempMap.put(field, fieldClazz);
                    map.put(objType, tempMap);
                }
                continue;
            }
            if (!isValidField(field)) {
                throw new ObjectNotSupportedException("Wrong fields, Please annonate the custom objects");
            }
        }
        return map;
    }

    private Map<String, String> getFieldMapping(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        LinkedHashMap<String, String> fmap = new LinkedHashMap<>();
        for (Field field : fields) {
            Annotation anno;
            anno = field.getAnnotation(FieldMap.class) == null ? null : field.getAnnotation(FieldMap.class);
            if (anno != null) {
                anno = (FieldMap) anno;
                String fmStr = ((FieldMap) anno).fieldname();
                fmap.put(field.getName(), fmStr);
            }
        }
        return fmap;
    }

    private String getInitialField(Class classname) {
        Field[] fields = classname.getDeclaredFields();
        Field InitailField = fields[0];
        LinkedHashMap<String, String> fmap = new LinkedHashMap<>();
        for (Field field : fields) {
            Class<?> fieldclazz = field.getType();
            Annotation anno;
            anno = field.getAnnotation(RootElement.class) == null ? null : field.getAnnotation(RootElement.class);
            if (anno != null) {
                anno = field.getAnnotation(FieldMap.class);
                anno = (FieldMap) anno;
                if (anno != null) {
                    return ((FieldMap) anno).fieldname();
                }
                return field.getName();
            }

        }
        Annotation anno;
        anno = InitailField.getAnnotation(FieldMap.class);
        anno = (FieldMap) anno;
        if (anno != null) {
            return ((FieldMap) anno).fieldname();
        }
        return InitailField.getName();
    }

    private boolean isValidField(Field field) {
        Class<?> clazz = field.getType();
        DA_TYPES da_type = DA_TYPES.getFromClazz(clazz);
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

    private boolean isPremitive(Class<?> clazz) {
        DA_TYPES da_type = DA_TYPES.getFromClazz(clazz);
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


    private boolean isObject(String key, String fieldName) {
        validateFieldKeyMatch(key, fieldName);
        if (getFieldKeyIndex(key, fieldName) < key.length())
            return key.charAt(getFieldKeyIndex(key, fieldName)) == '.' ? true : false;
        return false;
    }

    private boolean isArray(String key, String fieldName) {
        validateFieldKeyMatch(key, fieldName);
        if (getFieldKeyIndex(key, fieldName) < key.length())
            return key.charAt(getFieldKeyIndex(key, fieldName)) == '[' ? true : false;
        return false;
    }
    private boolean isInitalArray(Class classname) {
        Field[] fields = classname.getDeclaredFields();
        Field InitailField = fields[0];
        LinkedHashMap<String, String> fmap = new LinkedHashMap<>();
        for (Field field : fields) {
            Class<?> fieldclazz = field.getType();
            Annotation anno;
            anno = field.getAnnotation(RootElement.class) == null ? null : field.getAnnotation(RootElement.class);
            if (anno != null) {
                anno = field.getAnnotation(RjsonArray.class);
                if (anno != null) {
                    return true;
                }
                return false;
            }

        }
        Annotation anno;
        anno = InitailField.getAnnotation(RjsonArray.class);
        if (anno != null) {
            return true;
        }
        return false;
    }

    private void validateFieldKeyMatch(String key, String field) {
        if (!key.toLowerCase().contains(field.toLowerCase())) {
            throw new FieldandKeyMatchException(key + " and " + field + " does not match");
        }
    }


    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        JsonParserImpl jsonParser = new JsonParserImpl();
        //jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\Array.json"));
        //jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\Emp.json"));
        //jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\hotels_response_API.json"));
        //jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\gitsamplebigjson.json"));
        //C:\Users\Praveen\Documents\hotel_api.json
        jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\hotel_api.json"));
        LinkedHashMap<Character, JsonKeys> l = jsonParser.getKeyDataStore();
        JMarshall<Model1Array> jMarshall = new JMarshall();
        jMarshall.setJsonParser(jsonParser);
        jMarshall.parser(Model1Array.class);
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
