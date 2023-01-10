package org.vrp.utils.common;

import org.testng.annotations.Test;
import org.vrp.utils.Model1;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class JMarshallTest {
    JsonParserImpl jsonParser=null;
    JMarshall jMarshall=null;
    public void init(){
        JsonParserImpl jsonParser = new JsonParserImpl();

        jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\Array.json"));
        jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\hotels_response_API.json"));
        //jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\gitsamplebigjson.json"));
        LinkedHashMap<Character, JsonKeys> l = jsonParser.getKeydatastore();
        JMarshall jMarshall=new JMarshall(new Model1());
    }
    @Test
    public void getFieldIndexTest(){
        init();
        jMarshall.setJsonParser(jsonParser);
        try {
            jMarshall.parser(Model1.class);
        } catch (ClassNotFoundException e) {
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