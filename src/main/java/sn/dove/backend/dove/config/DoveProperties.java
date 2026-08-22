package sn.dove.backend.dove.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dove")
public class DoveProperties {

    private final Seed seed = new Seed();
    private final Auth auth = new Auth();
    private final Storage storage = new Storage();

    public Seed getSeed() {
        return seed;
    }

    public Auth getAuth() {
        return auth;
    }

    public Storage getStorage() {
        return storage;
    }

    public static class Seed {

        private boolean enabled;
        private String location = "file:../DoveFront/db.json";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    public static class Auth {

        private boolean devHeaderEnabled;
        private String defaultUserId = "85006628-0dd6-5586-a35c-b1cb40b9de1f";

        public boolean isDevHeaderEnabled() {
            return devHeaderEnabled;
        }

        public void setDevHeaderEnabled(boolean devHeaderEnabled) {
            this.devHeaderEnabled = devHeaderEnabled;
        }

        public String getDefaultUserId() {
            return defaultUserId;
        }

        public void setDefaultUserId(String defaultUserId) {
            this.defaultUserId = defaultUserId;
        }
    }

    public static class Storage {

        private String provider = "local";
        private Path localDirectory = Path.of(".dove-storage");
        private String gatewayBaseUrl;
        private String gatewayApiKey;
        private String publicBaseUrl = "http://localhost:8080";
        private long maxUploadBytes = 536_870_912L;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public Path getLocalDirectory() {
            return localDirectory;
        }

        public void setLocalDirectory(Path localDirectory) {
            this.localDirectory = localDirectory;
        }

        public String getGatewayBaseUrl() {
            return gatewayBaseUrl;
        }

        public void setGatewayBaseUrl(String gatewayBaseUrl) {
            this.gatewayBaseUrl = gatewayBaseUrl;
        }

        public String getGatewayApiKey() {
            return gatewayApiKey;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(String publicBaseUrl) {
            this.publicBaseUrl = publicBaseUrl;
        }

        public void setGatewayApiKey(String gatewayApiKey) {
            this.gatewayApiKey = gatewayApiKey;
        }

        public long getMaxUploadBytes() {
            return maxUploadBytes;
        }

        public void setMaxUploadBytes(long maxUploadBytes) {
            this.maxUploadBytes = maxUploadBytes;
        }
    }
}
