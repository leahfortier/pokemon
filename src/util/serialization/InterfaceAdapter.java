package util.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import main.Global;

import java.lang.reflect.Type;

class InterfaceAdapter implements JsonSerializer, JsonDeserializer {
    private static final String CLASSNAME = "className";

    @Override
    public Object deserialize(JsonElement jsonElement,
                              Type type,
                              JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonPrimitive jsonPrimitive = (JsonPrimitive)jsonObject.get(CLASSNAME);
        jsonObject.remove(CLASSNAME);

        String className = jsonPrimitive.getAsString();
        Class classy = getObjectClass(className);

        return jsonDeserializationContext.deserialize(jsonObject, classy);
    }

    @Override
    public JsonElement serialize(Object toSerialize, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = jsonSerializationContext.serialize(toSerialize).getAsJsonObject();
        if (jsonObject.has(CLASSNAME)) {
            Global.error("Json Object already has class name element.");
        }
        jsonObject.addProperty(CLASSNAME, toSerialize.getClass().getName());
        return jsonObject;
    }

    // Helper method to get the className of the object to be deserialized
    private Class getObjectClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e.getMessage());
        }
    }
}
