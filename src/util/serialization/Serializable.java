package util.serialization;

import main.Global;
import util.string.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

public interface Serializable extends java.io.Serializable {

    // Returns the serialized string representation of the current object
    default String serialize() {
        try (ByteArrayOutputStream sout = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(sout)) {
            out.writeObject(this);
            return Base64.getEncoder().encodeToString(sout.toByteArray());
        } catch (IOException exception) {
            Global.error("IOException occurred while serializing object " + this + ": " + exception);
            return StringUtils.empty();
        }
    }

    // Returns a new serialized copy of the current object
    default <T extends Serializable> T getSerializedCopy(Class<T> classy) {
        return deserialize(this.serialize(), classy);
    }

    // Serializes an object to the specified file
    default void serializeToFile(String fileName) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(this);
        } catch (IOException exception) {
            Global.error("IOException occurred while writing object to " + fileName + ": " + exception);
        }
    }

    // Deserialize data from a file to a serializable object
    static <T extends Serializable> T fromFile(File file, Class<T> classy) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return classy.cast(in.readObject());
        } catch (IOException | ClassNotFoundException | ClassCastException exception) {
            Global.error("Error deserializing from file " + file.getName() + ": " + exception);
            return null;
        }
    }

    // Deserializes the input string into a serializable object
    static <T extends Serializable> T deserialize(String serialized, Class<T> classy) {
        byte[] data = Base64.getDecoder().decode(serialized);
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return classy.cast(in.readObject());
        } catch (IOException | ClassNotFoundException | ClassCastException exception) {
            Global.error("Error deserializing from string " + serialized + ": " + exception);
            return null;
        }
    }
}
