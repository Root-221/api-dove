package sn.dove.backend.dove.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import sn.dove.backend.dove.service.DoveResourceStore;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class DoveSeedLoader implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DoveSeedLoader.class);

    private final DoveProperties properties;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final DoveResourceStore store;

    public DoveSeedLoader(DoveProperties properties, ResourceLoader resourceLoader, ObjectMapper objectMapper, DoveResourceStore store) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.store = store;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        loadSeeds(false);
    }

    public int loadSeeds(boolean force) throws IOException {
        if (!properties.getSeed().isEnabled() || (!force && store.hasAny("utilisateurs"))) {
            return 0;
        }

        Resource resource = resourceLoader.getResource(properties.getSeed().getLocation());
        if (!resource.exists()) {
            LOG.warn("DOVE seed is enabled but {} does not exist", properties.getSeed().getLocation());
            return 0;
        }

        int imported = 0;
        try (InputStream stream = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(stream);
            Iterator<Map.Entry<String, JsonNode>> fields = root.properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getKey().startsWith("$") || field.getValue().isNull()) {
                    continue;
                }
                if (field.getValue().isArray()) {
                    for (JsonNode item : field.getValue()) {
                        store.upsert(field.getKey(), item);
                        imported++;
                    }
                } else if (field.getValue().isObject() && !field.getValue().isEmpty()) {
                    store.upsert(field.getKey(), field.getValue());
                    imported++;
                }
            }
        }
        LOG.info("Imported {} DOVE seed resources into the application database", imported);
        return imported;
    }
}
