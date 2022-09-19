package fr.bvedrenne.io;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import fr.bvedrenne.database.DatabaseInformation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Configuration {
    private Path configFile = Paths.get(".database.json");
    private List<DatabaseInformation> databaseInformations = Collections.EMPTY_LIST;

    private static Configuration instance = new Configuration();

    public static Configuration getInstance() {
        return instance;
    }

    private Configuration() {
        if(!Files.exists(configFile)) {
            try {
                Files.createFile(configFile);
                Files.writeString(configFile, "[]");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ObjectMapper mapper = new JsonMapper();
            try (InputStream in = Files.newInputStream(configFile)) {
                databaseInformations = mapper.readValue(in, new TypeReference<List<DatabaseInformation>>() {});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDatabase(DatabaseInformation databaseInformation) {
        ObjectMapper mapper = new JsonMapper();

        databaseInformations = new ArrayList<>(databaseInformations);
        databaseInformations.add(databaseInformation);

        try (OutputStream out = Files.newOutputStream(configFile)) {
            mapper.writeValue(out, databaseInformations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<DatabaseInformation> getDatabaseInformations() {
        return databaseInformations;
    }
}
