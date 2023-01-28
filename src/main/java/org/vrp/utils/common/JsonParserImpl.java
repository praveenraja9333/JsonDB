package org.vrp.utils.common;


import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.*;
import java.util.LinkedHashMap;

public class JsonParserImpl {
    private JsonReader reader;
    private String root = "";
    private LinkedHashMap<String, String> temp = new LinkedHashMap<>();
    private LinkedHashMap<Character, JsonKeys> keydatastore = new LinkedHashMap<>();

    public void init(File JsonFile) {
        try {
            root = "";
            this.reader = new JsonReader(new FileReader(JsonFile));
            JsonToken token = reader.peek();
            if (token.equals(JsonToken.BEGIN_ARRAY)) {
                handleArray();
            } else {
                parse();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void init(String JsonString) {
        try {
            root = "";
            ByteArrayInputStream _barray = new ByteArrayInputStream(JsonString.getBytes());
            this.reader = new JsonReader(new InputStreamReader(_barray));
            JsonToken token = reader.peek();
            if (token.equals(JsonToken.BEGIN_ARRAY)) {
                handleArray();
            } else {
                parse();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addKeystore(String key) {
        JsonKeys jsonKeys = keydatastore.getOrDefault(key.charAt(0), new JsonKeys());
        jsonKeys.add(key);
        keydatastore.put(key.charAt(0), jsonKeys);
    }

    private void parse() throws IOException {
        reader.beginObject();
        String name = "";
        while (true) {
            JsonToken token = reader.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    root = name;
                    handleArray();
                    break;
                case BEGIN_OBJECT:
                    root = name;
                    parse();
                    break;
                case NAME:
                    name = reader.nextName();
                    name = root.equals("") ? name : root + "." + name;
                    break;
                case STRING:
                    String value = reader.nextString();
                    addKeystore(name);
                    temp.put(name, value);
                    break;
                case NUMBER:
                    value = String.valueOf(reader.nextLong());
                    addKeystore(name);
                    temp.put(name, value);
                    break;
                case END_OBJECT:
                    reader.endObject();
                    if (!root.equals("") && root.contains(".") && ']' != root.charAt(root.length() - 1)) {
                        root = root.substring(0, root.lastIndexOf("."));
                    } else if (!root.equals("") && ']' != root.charAt(root.length() - 1)) {
                        root = "";
                    } else if (!root.equals("") && ']' == root.charAt(root.length() - 1)) {
                        root = root.substring(0, root.lastIndexOf("["));
                    }
                    return;
                case BOOLEAN:
                    value = String.valueOf(reader.nextBoolean());
                    addKeystore(name);
                    temp.put(name, value);
                    break;
                case NULL:
                    reader.nextNull();
                    break;
                case END_DOCUMENT:
                    return;
                default:
                    break;
            }

        }
    }

    private void handleArray() throws IOException {
        reader.beginArray();
        int counter = 0;
        String name = "";
        while (true) {
            JsonToken token = reader.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    root = name.equals("") ? root + "[" + (counter++) + "]" : name + "[" + (counter++) + "]";
                    handleArray();
                    break;
                case BEGIN_OBJECT:
                    root = name.equals("") ? root + "[" + (counter++) + "]" : name + "[" + (counter++) + "]";
                    parse();
                    break;
                case NAME:
                    name = reader.nextName();
                    name = root.equals("") ? name : root + "." + name;
                    break;
                case STRING:
                    String value = reader.nextString();
                    addKeystore(root + "[" + (counter) + "]");
                    temp.put(root + "[" + (counter++) + "]", value);
                    break;
                case NUMBER:
                    value = String.valueOf(reader.nextLong());
                    addKeystore(root + "[" + (counter) + "]");
                    temp.put(root + "[" + (counter++) + "]", value);
                    break;
                case END_ARRAY:
                    reader.endArray();
                    if (!root.equals("") && root.contains(".")) {
                        root = root.substring(0, root.lastIndexOf("."));
                    }
                    return;
                case NULL:
                    reader.nextNull();
                    break;
                case BOOLEAN:
                    value = String.valueOf(reader.nextBoolean());
                    addKeystore(root + ".[" + (counter++) + "]");
                    temp.put(root + ".[" + (counter++) + "]", value);
                    break;
                case END_DOCUMENT:
                    return;
                default:
                    return;
            }

        }
    }

    public LinkedHashMap<String, String> getTemp() {
        if (temp == null || temp.size() == 0) {
            throw (new RuntimeException("value cache is null, please parse the json file or string"));
        }
        return temp;
    }

    public LinkedHashMap<Character, JsonKeys> getKeyDataStore() {
        if (keydatastore == null || keydatastore.size() == 0) {
            throw (new RuntimeException("key value cache is null, please parse the json file or string"));
        }
        return keydatastore;
    }

    public static void main(String[] args) {
        JsonParserImpl jsonParser = new JsonParserImpl();
       /* jsonParser.init("[\n" +
                "    {\n" +
                "        \"name\": \"Jason\",\n" +
                "        \"gender\": \"M\",\n" +
                "        \"age\": 27\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Rosita\",\n" +
                "        \"gender\": \"F\",\n" +
                "        \"age\": 23\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"Leo\",\n" +
                "        \"gender\": \"M\",\n" +
                "        \"age\": 19\n" +
                "    }\n" +
                "]");  */
        //jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\Array.json"));
        //jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\hotels_response_API.json"));
        jsonParser.init(new File("C:\\Users\\Praveen\\Documents\\test9.json"));
        LinkedHashMap<Character, JsonKeys> l = jsonParser.keydatastore;
        // JMarshall jMarshall=new JMarshall();
    }
}
