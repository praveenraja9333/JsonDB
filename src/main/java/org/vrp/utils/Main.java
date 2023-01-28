package org.vrp.utils;
import org.vrp.utils.meta.RjsonObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        /*JsonParserImpl< "org.vrp.utils.Models.Model1"> jsonParser = new JsonParserImpl();
        JMarshall<Model1> marshaller=new JMarshall(Class.forName("org.vrp.utils.Models.Model1").getDeclaredConstructor().newInstance());
        jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\hotels_response_API.json"));
        marshaller.setJsonParser(jsonParser);*/
        //marshaller.parse();
        String classname="org.vrp.utils.Models.Model1";
        getClazz(Class.forName(classname));
    }
    public static void getClazz(Class<?> classname) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Field[] fields=classname.getDeclaredFields();
        for(Field field:fields){
            System.out.println("Field Name "+ field.getName()+" "+field.getType().getName());
            Annotation anno=field.getAnnotation(RjsonObject.class);
            System.out.println(anno!=null?anno.toString():null);
        }
    }

}