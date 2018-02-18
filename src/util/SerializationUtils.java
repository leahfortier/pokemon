package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import main.Global;
import map.condition.Condition;
import pattern.action.ActionMatcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

public class SerializationUtils {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Condition.class, new InterfaceAdapter())
            .registerTypeAdapter(ActionMatcher.class, new InterfaceAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setLenient()
            .create();

    public static <T> T deserializeJson(String jsonString, Class<T> classy) {
        return gson.fromJson(jsonString, classy);
    }

    public static String getJson(final Object jsonObject) {
        return gson.toJson(jsonObject)
                   .replaceAll("\\\\n", "\n")
                   .replaceAll("\\\\t", "\t");
    }

    public static Object deserializeFromFile(File file) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Global.error("Error deserializing from file " + file.getName() + ": " + e.getMessage());
            return file;
        }
    }

    public static void serializeToFile(String fileName, Serializable serializable) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(serializable);
        } catch (IOException exception) {
            Global.error("IOException occurred while writing object to " + fileName + ": " + exception.getMessage());
        }
    }

    public static Object deserialize(String serialized) {
        byte[] data = Base64.getDecoder().decode(serialized);
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Global.error("Error deserializing from string " + serialized + ": " + e.getMessage());
            return StringUtils.empty();
        }
    }

    public static String serialize(Serializable serializable) {
        try (ByteArrayOutputStream sout = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(sout)) {
            out.writeObject(serializable);
            return Base64.getEncoder().encodeToString(sout.toByteArray());
        } catch (IOException exception) {
            Global.error("IOException occurred while serializing object" + serializable.toString());
            return StringUtils.empty();
        }
    }

    public static Object getSerializedCopy(Serializable serializable) {
        return deserialize(serialize(serializable));
    }

    public static <T> T deserializeJsonFile(String fileName, Class<T> classy) {
        String jsonContents = FileIO.readEntireFileWithReplacements(fileName, false);

        T deserialized = SerializationUtils.deserializeJson(jsonContents, classy);
        JsonObject mappity = SerializationUtils.deserializeJson(jsonContents, JsonObject.class);

        String formattedJson = SerializationUtils.getJson(deserialized);
        String mapJson = SerializationUtils.getJson(mappity);

//        FileIO.writeToFile("out.txt", formattedJson);
//        FileIO.writeToFile("out2.txt", mapJson);

        if (!formattedJson.equals(mapJson)) {
            Global.error("No dice: " + fileName);
        }

        FileIO.overwriteFile(fileName, formattedJson);

        return deserialized;
    }
}
