# Jmix Plugin SDK. Стратегия интеграции

## Переход от TypeScript-прототипа к производственной реализации на Java

| Параметр | Значение |
|----------|----------|
| Версия документа | 1.0 |
| Дата | 1 февраля 2026 г. |
| Статус | спецификация для Java-реализации |

---

## Аннотация

Прототип Jmix Plugin SDK реализован на TypeScript и выполняет функцию концептуальной демонстрации проектируемой архитектуры. В настоящем документе формулируется стратегия переноса валидированной архитектуры в производственную Java-реализацию, интегрируемую с фреймворком Jmix.

---

## 1. Обоснование выбора TypeScript для прототипа

### 1.1. Преимущества прототипного подхода

Реализация прототипа на TypeScript обеспечивает:

1. Ускоренную итерацию по проектированию API без необходимости полной интеграции с Java-инфраструктурой.
2. Возможность валидации архитектурных решений до фиксации Java-имплементации.
3. Исполняемую документацию архитектуры в виде рабочего кода.
4. Независимость разработки от существующей кодовой базы Jmix.
5. Гибкость в рефакторинге и проверке альтернативных решений.

### 1.2. Объект демонстрации

Прототип демонстрирует следующие аспекты:

- архитектурные подходы:
    - управление жизненным циклом плагинов;
    - алгоритмы разрешения зависимостей;
    - проектирование контекстного API;
    - модель событийной шины;
- проектирование API:
    - структура базового класса плагина;
    - интерфейс менеджера плагинов;
    - сервисы контекста плагина;
    - паттерны обработчиков жизненного цикла;
- формат дескриптора:
    - JSON-схема файла `plugin.json`;
    - правила валидации;
    - спецификации совместимости.

---

## 2. Требования к Java-реализации

### 2.1. Ядро системы плагинов

#### 2.1.1. Базовый класс `JmixPlugin`

```java
package io.jmix.core.plugin;

import org.springframework.context.ApplicationContext;

/**
 * Base class for Jmix plugins.
 * Mirrors the TypeScript Plugin class but integrated with Spring.
 */
public abstract class JmixPlugin {

    private PluginDescriptor descriptor;
    private PluginContext context;
    private volatile boolean loaded = false;

    public abstract void onLoad(PluginContext context) throws PluginException;

    public abstract void onUnload() throws PluginException;

    public void onConfigChange(String key, Object value) throws PluginException {
        // default no-op
    }

    public String getId() {
        return descriptor != null ? descriptor.getId() : "unknown";
    }

    public String getName() {
        return descriptor != null ? descriptor.getName() : "Unknown Plugin";
    }

    public String getVersion() {
        return descriptor != null ? descriptor.getVersion() : "0.0.0";
    }

    public boolean isLoaded() {
        return loaded;
    }

    protected void markAsLoaded() {
        this.loaded = true;
    }

    protected void markAsUnloaded() {
        this.loaded = false;
    }

    void setDescriptor(PluginDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    void setContext(PluginContext context) {
        this.context = context;
    }

    protected PluginContext getContext() {
        return context;
    }
}
```

#### 2.1.2. Класс `PluginDescriptor`

```java
package io.jmix.core.plugin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Plugin descriptor matching plugin.json schema.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginDescriptor {

    @JsonProperty(required = true)
    private String id;

    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
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

    public static class Author {
        private String name;
        private String email;
        private String url;
    }

    public static class Compatibility {
        private String jmix;
        private String java;
        private String springBoot;
    }

    public static class Dependencies {
        private List<PluginDependency> plugins;
        private List<MavenDependency> maven;
    }
}
```

#### 2.1.3. Менеджер плагинов `JmixPluginManager`

```java
package io.jmix.core.plugin;

import io.jmix.core.JmixModules;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for Jmix plugins.
 * Integrates with existing JmixModules system.
 */
@Component("core_PluginManager")
public class JmixPluginManager {

    private final ApplicationContext applicationContext;
    private final JmixModules jmixModules;
    private final Map<String, JmixPlugin> plugins = new ConcurrentHashMap<>();
    private final Map<String, PluginDescriptor> descriptors = new ConcurrentHashMap<>();
    private final Map<String, PluginState> states = new ConcurrentHashMap<>();
    private final Map<String, URLClassLoader> classLoaders = new ConcurrentHashMap<>();

    public JmixPluginManager(ApplicationContext applicationContext, JmixModules jmixModules) {
        this.applicationContext = applicationContext;
        this.jmixModules = jmixModules;
    }

    public void loadPlugin(File pluginJar) throws PluginException {
        try {
            URLClassLoader classLoader = new URLClassLoader(
                new URL[]{pluginJar.toURI().toURL()},
                getClass().getClassLoader()
            );

            PluginDescriptor descriptor = readDescriptor(classLoader);

            validateDescriptor(descriptor);

            checkCompatibility(descriptor);

            checkDependencies(descriptor);

            Class<?> pluginClass = classLoader.loadClass(descriptor.getEntrypoints().getMain());
            JmixPlugin plugin = (JmixPlugin) pluginClass.getDeclaredConstructor().newInstance();

            plugin.setDescriptor(descriptor);
            PluginContext context = createPluginContext(descriptor.getId());
            plugin.setContext(context);

            plugins.put(descriptor.getId(), plugin);
            descriptors.put(descriptor.getId(), descriptor);
            classLoaders.put(descriptor.getId(), classLoader);
            states.put(descriptor.getId(), PluginState.LOADED);

            plugin.onLoad(context);
            plugin.markAsLoaded();

            applicationContext.publishEvent(new PluginLoadedEvent(this, descriptor.getId()));

        } catch (Exception e) {
            throw new PluginException("Failed to load plugin: " + e.getMessage(), e);
        }
    }

    public void unloadPlugin(String pluginId) throws PluginException {
        JmixPlugin plugin = plugins.get(pluginId);
        if (plugin == null) {
            throw new PluginException("Plugin not found: " + pluginId);
        }

        try {
            checkDependents(pluginId);

            plugin.onUnload();
            plugin.markAsUnloaded();

            URLClassLoader classLoader = classLoaders.remove(pluginId);
            if (classLoader != null) {
                classLoader.close();
            }

            plugins.remove(pluginId);
            descriptors.remove(pluginId);
            states.put(pluginId, PluginState.UNLOADED);

            applicationContext.publishEvent(new PluginUnloadedEvent(this, pluginId));

        } catch (Exception e) {
            throw new PluginException("Failed to unload plugin: " + e.getMessage(), e);
        }
    }

    private PluginContext createPluginContext(String pluginId) {
        return new JmixPluginContext(applicationContext, pluginId, jmixModules);
    }

    private void checkCompatibility(PluginDescriptor descriptor) throws PluginException {
        String platformVersion = jmixModules.getLast().getVersion();
        String requiredVersion = descriptor.getCompatibility().getJmix();

        if (!VersionUtils.satisfies(platformVersion, requiredVersion)) {
            throw new PluginException(
                String.format("Plugin %s requires Jmix %s, but platform version is %s",
                    descriptor.getId(), requiredVersion, platformVersion)
            );
        }
    }
}
```

### 2.2. Интеграция со Spring Boot

#### 2.2.1. Класс `PluginConfiguration`

```java
package io.jmix.core.plugin;

import io.jmix.core.JmixModules;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for plugin system.
 */
@Configuration
@ConditionalOnProperty(name = "jmix.plugin.enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(PluginProperties.class)
public class PluginConfiguration {

    @Bean("core_PluginManager")
    public JmixPluginManager pluginManager(
            ApplicationContext applicationContext,
            JmixModules jmixModules) {
        return new JmixPluginManager(applicationContext, jmixModules);
    }

    @Bean
    public PluginLoader pluginLoader(
            JmixPluginManager pluginManager,
            PluginProperties properties) {
        return new PluginLoader(pluginManager, properties);
    }

    @Bean
    public PluginRestController pluginRestController(JmixPluginManager pluginManager) {
        return new PluginRestController(pluginManager);
    }
}

/**
 * Configuration properties for plugin system.
 */
@ConfigurationProperties(prefix = "jmix.plugin")
public class PluginProperties {

    private boolean enabled = false;

    private List<String> locations = Arrays.asList(
        "classpath:plugins/",
        "file:${jmix.core.work-dir}/plugins/"
    );

    private boolean autoLoad = true;
}
```

### 2.3. Обратная совместимость с `@JmixModule`

#### 2.3.1. Паттерн адаптера

```java
package io.jmix.core.plugin;

import io.jmix.core.annotation.JmixModule;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

/**
 * Marks a @Configuration class as plugin-compatible.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JmixPluginAdapter {

    String descriptor() default "classpath:plugin.json";
}

@Configuration
@JmixModule(dependsOn = CoreConfiguration.class)
@JmixPluginAdapter
public class MyModuleConfiguration {
    // существующий код остаётся без изменений
}
```

### 2.4. Интеграция с Gradle

```groovy
plugins {
    id 'io.jmix' version '2.0.0'
    id 'io.jmix.plugin-sdk' version '1.0.0'
}

jmixPlugin {
    pluginId = 'io.jmix.plugins.advancedgrid'
    pluginName = 'Advanced Grid'
    pluginVersion = '2.1.0'
    mainClass = 'io.jmix.plugins.advancedgrid.AdvancedGridPlugin'

    compatibility {
        jmix = '>=2.0.0 <3.0.0'
        java = '>=17'
        springBoot = '>=3.0.0'
    }

    permissions = ['jmix.data.read', 'jmix.ui.component.register']
}

tasks.named('jar') {
    dependsOn 'generatePluginDescriptor'
}
```

---

## 3. Стратегия миграции

### 3.1. Этап 1. Сосуществование подсистем

Обе подсистемы расширяемости функционируют параллельно.

```
Classpath приложения
├── модули времени компиляции (@JmixModule)
│   ├── jmix-core.jar
│   ├── jmix-security.jar
│   └── my-custom-module.jar
│
└── плагины времени выполнения (JmixPlugin)
    ├── plugins/advanced-grid-2.1.0.jar
    ├── plugins/custom-chart-1.0.0.jar
    └── plugins/api-connector-3.2.0.jar
```

### 3.2. Этап 2. Постепенная миграция

Существующие модули могут опционально получать возможности плагинов.

```java
// Шаг 1. Добавление аннотации @JmixPluginAdapter
@Configuration
@JmixModule(dependsOn = CoreConfiguration.class)
@JmixPluginAdapter
public class MyModuleConfiguration {
    // изменения исходного кода не требуются
}

// Шаг 2. Добавление файла plugin.json
// src/main/resources/plugin.json
{
  "id": "com.example.mymodule",
  "name": "My Module",
  "version": "1.0.0"
}

// Шаг 3. Модуль допускает установку в качестве плагина и может загружаться
// как во время компиляции, так и во время выполнения.
```

### 3.3. Этап 3. Использование упрощённого API в новых плагинах

```java
public class SimplePlugin extends JmixPlugin {

    @Override
    public void onLoad(PluginContext context) {
        DataManager dm = context.getBean(DataManager.class);

        context.getComponentRegistry().register(
            "my-chart",
            MyChartComponent.class
        );

        context.on("entity:created", this::onEntityCreated);
    }

    private void onEntityCreated(EntityEvent event) {
        getContext().getLogger().info("Entity created: {}", event);
    }

    @Override
    public void onUnload() {
        // освобождение ресурсов
    }
}
```

---

## 4. Стратегия тестирования

### 4.1. Модульное тестирование

```java
@SpringBootTest
@ExtendWith(PluginTestExtension.class)
class MyPluginTest {

    @MockPlugin
    private MyPlugin plugin;

    @MockedPluginContext
    private PluginContext context;

    @Test
    void testPluginLoad() throws Exception {
        plugin.onLoad(context);

        verify(context.getComponentRegistry())
            .register(eq("my-component"), any());
    }
}
```

### 4.2. Интеграционное тестирование

```java
@JmixPluginTest
@TestPlugin("test-plugin-1.0.0.jar")
class PluginIntegrationTest {

    @Autowired
    private JmixPluginManager pluginManager;

    @Test
    void testPluginIntegration() {
        assertTrue(pluginManager.isLoaded("com.example.testplugin"));

        JmixPlugin plugin = pluginManager.get("com.example.testplugin");
        assertNotNull(plugin);
    }
}
```

---

## 5. Перечень работ для реализации

### 5.1. Ядро SDK (приоритет 1)

- сформировать пакет `io.jmix.core.plugin`;
- реализовать базовый класс `JmixPlugin`;
- реализовать `PluginDescriptor` с привязкой Jackson;
- реализовать `JmixPluginManager`;
- реализовать интерфейс `PluginContext`;
- реализовать проверку совместимости версий (semver);
- реализовать разрешение зависимостей;
- реализовать изоляцию через `ClassLoader`.

### 5.2. Интеграция со Spring (приоритет 1)

- реализовать автоконфигурацию `PluginConfiguration`;
- определить класс `PluginProperties`;
- интегрировать с `JmixModulesProcessor`;
- определить Spring-события (например, `PluginLoadedEvent`);
- разработать REST API управления плагинами.

### 5.3. Инструменты разработчика (приоритет 2)

- разработать Gradle-плагин для разработки плагинов;
- реализовать задачу `generatePluginDescriptor`;
- реализовать задачу упаковки плагина;
- подготовить Maven-архетип для проектов плагинов;
- разработать инструмент командной строки.

### 5.4. Интеграция с Jmix Studio (приоритет 2)

- добавить раздел «Plugins» в интерфейс Studio;
- разработать диалог установки плагина;
- разработать представление управления плагинами;
- реализовать обозреватель маркетплейса;
- реализовать уведомления об обновлениях плагинов.

### 5.5. Поддержка тестирования (приоритет 3)

- определить аннотацию `@JmixPluginTest`;
- реализовать расширение `PluginTestExtension` для JUnit 5;
- подготовить mock-реализации для тестирования;
- разработать вспомогательные утилиты для разработчиков плагинов.

### 5.6. Документация (приоритет 3)

- руководство разработчика плагинов;
- API-справочник (Javadoc);
- руководство по миграции (`@JmixModule` → `JmixPlugin`);
- учебный сценарий разработки первого плагина;
- видеоматериалы.

---

## 6. Критерии успеха

### 6.1. Функциональные требования

- загрузка плагинов из JAR-файлов во время выполнения;
- изоляция загрузчиков классов плагинов;
- корректное разрешение зависимостей;
- проверка совместимости версий;
- сохранение работоспособности существующих модулей `@JmixModule`;
- доступ плагинов к сервисам Jmix через `PluginContext`;
- регистрация плагинами UI-компонентов;
- выгрузка плагинов без перезапуска приложения.

### 6.2. Нефункциональные требования

- время загрузки одного плагина — менее 1 секунды;
- накладные расходы по памяти при 20 одновременных плагинах — менее 10 %;
- отсутствие деградации производительности для существующих модулей;
- невозможность доступа плагинов к неавторизованным ресурсам;
- отказ плагина не приводит к падению приложения.

### 6.3. Опыт разработчика

- время создания типового плагина не более 2 ч (против 4–8 ч в текущей версии прототипа);
- количество строк кода для базового плагина — не более 50 (против 150 в прототипе);
- количество шагов установки — не более 3 (против 8 в прототипе);
- покрытие документацией не менее 90 %.

---

## 7. Заключение

Прототип на TypeScript позволил валидировать архитектуру и проектируемый интерфейс. Дальнейший путь к производственной Java-реализации включает:

1. использование валидированных архитектурных подходов;
2. интеграцию с существующей инфраструктурой Jmix и Spring Boot;
3. сохранение обратной совместимости с механизмом `@JmixModule`;
4. упрощение модели разработки плагинов;
5. реализацию управления плагинами во время выполнения.

Двухуровневая архитектура (статические модули времени компиляции и динамические плагины времени выполнения) обеспечивает компромисс между стабильностью ядра и гибкостью пользовательских расширений.

---

## 8. Дальнейшие шаги

1. Сформировать структуру Java-пакета `io.jmix.core.plugin`.
2. Реализовать базовые классы: `JmixPlugin`, `JmixPluginManager`, `PluginContext`.
3. Подготовить автоконфигурацию Spring Boot.
4. Реализовать Gradle-плагин для разработки расширений.
5. Подготовить документацию разработчика.
6. Разработать набор демонстрационных плагинов.
7. Выполнить интеграцию с Jmix Studio.

Ориентировочная длительность работ первой фазы — 3–4 месяца.
