package org.vrp.utils.common;

import org.junit.Before;
import org.junit.Test;
import org.vrp.utils.Models.Model1;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class JMarshallTest {
    JsonParserImpl jsonParser=null;
    JMarshall jMarshall=null;
    @Before
    public void init(){
        JsonParserImpl jsonParser = new JsonParserImpl();

        jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\Array.json"));
        jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\hotels_response_API.json"));
        //jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\gitsamplebigjson.json"));
        LinkedHashMap<Character, JsonKeys> l = jsonParser.getKeydatastore();
        jMarshall=new JMarshall(new Model1());
    }
   @Test
    public void getFieldIndexTest(){
        jMarshall.setJsonParser(jsonParser);
        try {
            jMarshall.parser(Model1.class);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        String Key="Hello.Hi.Bye";
        String field="Hi";
        //assertEquals(9,jMarshall.getFieldIndex(Key,field));
        assertEquals('.',Key.charAt(jMarshall.getFieldKeyIndex(Key,field)));
    }
    public void loadJson(){

    }


}