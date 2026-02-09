package io.jmix.plugin.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties bound to the {@code jmix.plugin} prefix.
 * Enabled via {@link PluginAutoConfiguration}.
 */
@ConfigurationProperties(prefix = "jmix.plugin")
public class PluginProperties {

    /** Enable plugin SDK auto-configuration. */
    private boolean enabled = true;

    /** Eagerly load all registered plugins after application startup. */
    private boolean autoLoad = true;

    /** Reported platform version used for compatibility checks. */
    private String platformVersion = "2.0.0";

    /** Filesystem locations scanned for plugin JARs at startup. */
    private List<String> locations = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAutoLoad() {
        return autoLoad;
    }

    public void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations == null ? new ArrayList<>() : locations;
    }
}
