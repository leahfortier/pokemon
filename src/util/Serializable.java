package util;

import main.Global;

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
            Global.error("IOException occurred while serializing object " + this + ": " + exception.getMessage());
            return StringUtils.empty();
        }
    }

    // Returns a new serialized copy of the current object
    default Serializable getSerializedCopy() {
        return deserialize(this.serialize());
    }

    // Serializes an object to the specified file
    default void serializeToFile(String fileName) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(this);
        } catch (IOException exception) {
            Global.error("IOException occurred while writing object to " + fileName + ": " + exception.getMessage());
        }
    }

    // Deserialize data from a file to a serializable object
    static Serializable fromFile(File file) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (Serializable)in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Global.error("Error deserializing from file " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }

    // Deserializes the input string into a serializable object
    static Serializable deserialize(String serialized) {
        byte[] data = Base64.getDecoder().decode(serialized);
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (Serializable)in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Global.error("Error deserializing from string " + serialized + ": " + e.getMessage());
            return null;
        }
    }
}
