package app.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // Register the module to support Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());
    }

    public static <T> List<T> readJsonFile(String filePath, Class<T[]> clazz) throws IOException {
        return List.of(mapper.readValue(new File(filePath), clazz));
    }

    public static <T> void writeJsonFile(String filePath, List<T> data) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), data);
    }
}
