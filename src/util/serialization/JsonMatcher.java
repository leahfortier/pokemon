package util.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import main.Global;
import map.condition.Condition;
import pattern.action.ActionMatcher;
import util.FileIO;

public interface JsonMatcher {
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(Condition.class, new InterfaceAdapter())
            .registerTypeAdapter(ActionMatcher.class, new InterfaceAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setLenient()
            .create();

    default String getJson() {
        return getJson(this);
    }

    default <T> T getJsonCopy(Class<T> classy) {
        return deserialize(this.getJson(), classy);
    }

    static String getJson(Object jsonObject) {
        return gson.toJson(jsonObject)
                   .replaceAll("\\\\n", "\n")
                   .replaceAll("\\\\t", "\t");
    }

    static <T> T deserialize(String jsonString, Class<T> classy) {
        return gson.fromJson(jsonString, classy);
    }

    // Deserializes the specified json file into the specified class
    static <T extends JsonMatcher> T fromFile(String fileName, Class<T> classy) {
        String jsonContents = FileIO.readEntireFileWithReplacements(fileName, false);

        T deserialized = JsonMatcher.deserialize(jsonContents, classy);
        JsonObject mappity = JsonMatcher.deserialize(jsonContents, JsonObject.class);

        String formattedJson = JsonMatcher.getJson(deserialized);
        String mapJson = JsonMatcher.getJson(mappity);

//        FileIO.writeToFile("out.txt", formattedJson);
//        FileIO.writeToFile("out2.txt", mapJson);

        if (!formattedJson.equals(mapJson)) {
            Global.error("No dice: " + fileName);
        }

        FileIO.overwriteFile(fileName, formattedJson);

        return deserialized;
    }
}
