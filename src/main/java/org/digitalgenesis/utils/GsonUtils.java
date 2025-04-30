package org.digitalgenesis.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utilities for interacting with JSON and JSON files.
 */
public class GsonUtils {
    public static final Gson GLOBAL_GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Deserializes a JSON file to a Java object.
     * @param path The path to the JSON file
     * @param tClass The class of the Java object to deserialize to
     * @return The deserialized Java object
     */
    public static <T> T jsonFileToObject(String path, Class<T> tClass) {
        try {
            return new Gson().fromJson(new FileReader(path), tClass);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Serializes a Java object to a JSON file.
     * @param path The path to the JSON file
     * @param object The Java object to serialize
     */
    public static void objectToJsonFile(String path, Object object) {
        File dir = new File(path).getParentFile();
        if (dir != null) dir.mkdirs();

        try (Writer writer = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(object, writer);
        } catch (IOException ignored) {}
    }
}
