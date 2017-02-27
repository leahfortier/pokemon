package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import main.Global;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

public class SerializationUtils {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Double.class, (JsonSerializer<Double>) (source, sourceType, context) -> {
                if (source == source.longValue()) {
                    return new JsonPrimitive(source.longValue());
                } else {
                    return new JsonPrimitive(source);
                }
            })
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setLenient()
            .create();

    public static <T> T deserializeJson(String jsonString, Class<T> tClass) {
        return gson.fromJson(jsonString, tClass);
    }

    public static String getJson(final Object jsonObject) {
        return gson.toJson(jsonObject)
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\t", "\t");
    }

    public static Object deserializeFromFile(String fileName) {
        try {
            FileInputStream fin = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fin);

            Object object = in.readObject();

            in.close();
            fin.close();

            return object;
        } catch (IOException | ClassNotFoundException e) {
            Global.error("Error deserializing from file " + fileName);
            return fileName;
        }
    }

    public static void serializeToFile(String fileName, Serializable serializable) {
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(serializable);

            out.close();
            fout.close();
        } catch (IOException exception) {
            Global.error("IOException occurred while writing object to " + fileName + ".");
        }
    }

    public static Object deserialize(String serialized) {
        try {
            byte[] data = Base64.getDecoder().decode(serialized);
            ByteArrayInputStream sin = new ByteArrayInputStream(data);
            ObjectInputStream in = new ObjectInputStream(sin);

            Object object = in.readObject();

            in.close();
            sin.close();

            return object;
        } catch (IOException | ClassNotFoundException e) {
            Global.error("Error deserializing from string " + serialized);
            return StringUtils.empty();
        }
    }

    public static String serialize(Serializable serializable) {
        try {
            ByteArrayOutputStream sout = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(sout);

            out.writeObject(serializable);

            out.close();
            sout.close();

            return Base64.getEncoder().encodeToString(sout.toByteArray());
        } catch (IOException exception) {
            Global.error("IOException occurred while serializing object" + serializable.toString());
            return StringUtils.empty();
        }
    }
}
