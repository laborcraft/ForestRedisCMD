package cz.foresttech.forestredis.velocity.adapter;

import cz.foresttech.forestredis.shared.adapter.IConfigurationAdapter;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IConfigurationAdapter} for Bungee version
 */
public class VelocityConfigAdapter implements IConfigurationAdapter {

    private String fileName;
    private final Path dataDirectory;
    private final Logger logger;
    private Map<String, Object> config = null;

    /**
     * Constructs the instance of adapter.
     *
     * @param dataDirectory {@link Path} directory of the plugin
     * @param logger {@link Logger} logger
     */
    public VelocityConfigAdapter(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    @Override
    public void setup(String fileName) {
        this.fileName = fileName;
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            Path configPath = dataDirectory.resolve(fileName + ".yml");
            if (!Files.exists(configPath)) {
                try (InputStream in = getClass().getResourceAsStream("/" + fileName + ".yml")) {
                    Files.copy(in, configPath);
                }
            }
            loadConfiguration();
        } catch (Exception e) {
            logger.warning("Failed to setup configuration: " + e.getMessage());
        }
    }

    @Override
    public boolean isSetup() {
        return config != null;
    }

    @Override
    public void loadConfiguration() {
        try {
            Yaml yaml = new Yaml();
            Path configPath = dataDirectory.resolve(fileName + ".yml");
            try (InputStream input = Files.newInputStream(configPath)) {
                config = yaml.load(input);
            }
        } catch (Exception e) {
            logger.warning("Failed to load configuration: " + e.getMessage());
            config = new HashMap<>();
        }
    }

    private Object get(String path) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = config;

        for (int i = 0; i < parts.length - 1; i++) {
            Object obj = current.get(parts[i]);
            if (!(obj instanceof Map)) return null;
            current = (Map<String, Object>) obj;
        }
        return current.get(parts[parts.length - 1]);
    }

    @Override
    public String getString(String path, String def) {
        Object val = get(path);
        return val != null ? val.toString() : def;
    }

    @Override
    public int getInt(String path, int def) {
        Object val = get(path);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return def;
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        Object val = get(path);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        return def;
    }

    @Override
    public List<String> getStringList(String path) {
        Object val = get(path);
        if (val instanceof List) {
            return ((List<?>) val).stream()
                    .filter(item -> item instanceof String)
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Map<?, ?>> getMapList(String path) {
        Object val = get(path);
        if (val instanceof List) {
            return ((List<?>) val).stream()
                    .filter(item -> item instanceof Map)
                    .map(item -> (Map<?, ?>) item)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
