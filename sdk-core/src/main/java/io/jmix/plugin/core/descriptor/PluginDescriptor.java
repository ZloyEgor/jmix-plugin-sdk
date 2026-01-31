package io.jmix.plugin.core.descriptor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Plain Java projection of the {@code plugin.json} descriptor format
 * defined by the TypeScript prototype. The class is intentionally a
 * mutable POJO for Jackson interoperability; consumers should treat the
 * data as effectively immutable.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginDescriptor {

    private String id;
    private String name;
    private String version;
    private String description;

    private Author author;
    private License license;
    private Compatibility compatibility;
    private Dependencies dependencies;
    private List<Component> components;
    private List<String> permissions;
    private Entrypoints entrypoints;
    private Configuration configuration;
    private Marketplace marketplace;
    private Repository repository;
    private String documentation;
    private String changelog;
    private Support support;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Author getAuthor() { return author; }
    public void setAuthor(Author author) { this.author = author; }

    public License getLicense() { return license; }
    public void setLicense(License license) { this.license = license; }

    public Compatibility getCompatibility() { return compatibility; }
    public void setCompatibility(Compatibility compatibility) { this.compatibility = compatibility; }

    public Dependencies getDependencies() { return dependencies; }
    public void setDependencies(Dependencies dependencies) { this.dependencies = dependencies; }

    public List<Component> getComponents() { return components; }
    public void setComponents(List<Component> components) { this.components = components; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public Entrypoints getEntrypoints() { return entrypoints; }
    public void setEntrypoints(Entrypoints entrypoints) { this.entrypoints = entrypoints; }

    public Configuration getConfiguration() { return configuration; }
    public void setConfiguration(Configuration configuration) { this.configuration = configuration; }

    public Marketplace getMarketplace() { return marketplace; }
    public void setMarketplace(Marketplace marketplace) { this.marketplace = marketplace; }

    public Repository getRepository() { return repository; }
    public void setRepository(Repository repository) { this.repository = repository; }

    public String getDocumentation() { return documentation; }
    public void setDocumentation(String documentation) { this.documentation = documentation; }

    public String getChangelog() { return changelog; }
    public void setChangelog(String changelog) { this.changelog = changelog; }

    public Support getSupport() { return support; }
    public void setSupport(Support support) { this.support = support; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String name;
        private String email;
        private String url;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class License {
        private String type;
        private String url;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Compatibility {
        private String jmix;
        private String java;
        @JsonProperty("spring-boot")
        private String springBoot;

        public String getJmix() { return jmix; }
        public void setJmix(String jmix) { this.jmix = jmix; }
        public String getJava() { return java; }
        public void setJava(String java) { this.java = java; }
        public String getSpringBoot() { return springBoot; }
        public void setSpringBoot(String springBoot) { this.springBoot = springBoot; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Dependencies {
        private List<PluginDependency> plugins;
        private List<MavenDependency> maven;

        public List<PluginDependency> getPlugins() { return plugins; }
        public void setPlugins(List<PluginDependency> plugins) { this.plugins = plugins; }
        public List<MavenDependency> getMaven() { return maven; }
        public void setMaven(List<MavenDependency> maven) { this.maven = maven; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PluginDependency {
        private String id;
        private String version;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MavenDependency {
        private String groupId;
        private String artifactId;
        private String version;

        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }
        public String getArtifactId() { return artifactId; }
        public void setArtifactId(String artifactId) { this.artifactId = artifactId; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Component {
        private String tag;
        @JsonProperty("class")
        private String className;
        private String icon;
        private String description;

        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entrypoints {
        private String main;
        private String config;

        public String getMain() { return main; }
        public void setMain(String main) { this.main = main; }
        public String getConfig() { return config; }
        public void setConfig(String config) { this.config = config; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Configuration {
        private String schema;
        private Map<String, Object> defaults;

        public String getSchema() { return schema; }
        public void setSchema(String schema) { this.schema = schema; }
        public Map<String, Object> getDefaults() { return defaults; }
        public void setDefaults(Map<String, Object> defaults) { this.defaults = defaults; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Marketplace {
        private List<String> categories;
        private List<String> keywords;
        private String icon;
        private List<String> screenshots;
        private String demo;

        public List<String> getCategories() { return categories; }
        public void setCategories(List<String> categories) { this.categories = categories; }
        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public List<String> getScreenshots() { return screenshots; }
        public void setScreenshots(List<String> screenshots) { this.screenshots = screenshots; }
        public String getDemo() { return demo; }
        public void setDemo(String demo) { this.demo = demo; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private String type;
        private String url;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Support {
        private String email;
        private String forum;
        private String issues;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getForum() { return forum; }
        public void setForum(String forum) { this.forum = forum; }
        public String getIssues() { return issues; }
        public void setIssues(String issues) { this.issues = issues; }
    }
}
