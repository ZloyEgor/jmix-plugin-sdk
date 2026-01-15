# Сопоставление прототипа и производственной реализации

## TypeScript-демонстрация и Java-реализация: различия и стратегия переноса

| Параметр | Значение |
|----------|----------|
| Версия документа | 1.0 |
| Дата | 1 февраля 2026 г. |

---

## 1. Текущее состояние работ

### 1.1. Реализованная функциональность (TypeScript-прототип)

```typescript
// jmix-plugin-sdk/core/src/plugin/Plugin.ts
export abstract class Plugin {
    async onLoad(context: PluginContext): Promise<void>;
    async onUnload(): Promise<void>;
}
```

Назначение прототипа — концептуальная демонстрация. Прототип:

- иллюстрирует архитектуру подсистемы плагинов;
- демонстрирует проектируемый API;
- обеспечивает быструю итерацию по проектированию;
- служит документацией использованных подходов.

### 1.2. Функциональность, требующая Java-реализации

```java
// Требуется реализация в jmix/jmix-core/
package io.jmix.core.plugin;

public abstract class JmixPlugin {
    public abstract void onLoad(PluginContext context) throws PluginException;
    public abstract void onUnload() throws PluginException;
}
```

Назначение Java-реализации — интеграция с Jmix:

- работа в инфраструктуре Spring Boot;
- интеграция с существующей системой модулей;
- загрузка JAR-файлов во время выполнения;
- применение в производственных средах.

---

## 2. Сравнительная характеристика реализаций

| Аспект | TypeScript-прототип | Java-реализация |
|--------|---------------------|-----------------|
| Язык | TypeScript/JavaScript | Java 17 и выше |
| Назначение | демонстрация архитектуры | производственная интеграция с Jmix |
| Степень интеграции | автономный артефакт | встраивается в `io.jmix.core` |
| Загрузка плагинов | имитация | загрузка JAR через `ClassLoader` |
| Интеграция со Spring | отсутствует | полная интеграция с `ApplicationContext` |
| Взаимодействие с `@JmixModule` | вне существующей системы | интеграция с `JmixModulesProcessor` |
| Тестирование | Jest | JUnit 5, Spring Test |
| Применение | спецификация и обучение | производственный код |
| Статус | реализован как прототип | требуется разработка |

---

## 3. Архитектура интеграции

### 3.1. Двухуровневая модель расширяемости

```
┌─────────────────────────────────────────────────────┐
│         Приложение Jmix                             │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Уровень 1. Статические модули (существующий)      │
│  ┌───────────────────────────────────────┐         │
│  │ @JmixModule (compile-time)            │         │
│  │ - io.jmix.core.CoreConfiguration      │         │
│  │ - io.jmix.security.SecurityConfig     │         │
│  │ - com.myapp.MyModuleConfiguration     │         │
│  └───────────────────────────────────────┘         │
│           ↓ функциональность не изменяется         │
│  ┌───────────────────────────────────────┐         │
│  │ JmixModulesProcessor                  │         │
│  │ - обнаружение @JmixModule             │         │
│  │ - построение графа зависимостей       │         │
│  │ - регистрация в Spring                │         │
│  └───────────────────────────────────────┘         │
│                                                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Уровень 2. Динамические плагины (проектируемый)   │
│  ┌───────────────────────────────────────┐         │
│  │ JmixPlugin (runtime)                  │         │
│  │ - plugins/advanced-grid.jar           │         │
│  │ - plugins/custom-chart.jar            │         │
│  │ - plugins/api-connector.jar           │         │
│  └───────────────────────────────────────┘         │
│           ↓ проектируемая подсистема               │
│  ┌───────────────────────────────────────┐         │
│  │ JmixPluginManager                     │         │
│  │ - чтение plugin.json из JAR           │         │
│  │ - создание ClassLoader                │         │
│  │ - внедрение Spring-зависимостей       │         │
│  │ - вызов onLoad()                      │         │
│  └───────────────────────────────────────┘         │
│           ↓ точка интеграции                       │
│  ┌───────────────────────────────────────┐         │
│  │ JmixModules (существующий компонент)  │         │
│  │ - доступ к зарегистрированным модулям │         │
│  │ - использование Spring beans          │         │
│  └───────────────────────────────────────┘         │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 4. Принципы интеграции

### 4.1. Использование существующей инфраструктуры

```java
public class JmixPluginManager {
    private final ApplicationContext applicationContext;  // существующий Spring-контекст
    private final JmixModules jmixModules;                // существующая система модулей

    public JmixPluginManager(ApplicationContext ctx, JmixModules modules) {
        this.applicationContext = ctx;
        this.jmixModules = modules;
    }

    private PluginContext createContext(String pluginId) {
        return new JmixPluginContext(
            applicationContext,
            jmixModules,
            pluginId
        );
    }
}
```

### 4.2. Доступ к существующим сервисам

```java
public class MyPlugin extends JmixPlugin {
    @Override
    public void onLoad(PluginContext context) {
        DataManager dm = context.getBean(DataManager.class);
        Metadata metadata = context.getBean(Metadata.class);
        Messages messages = context.getBean(Messages.class);

        List<User> users = dm.load(User.class).all().list();

        context.getComponentRegistry().register("my-grid", MyGridComponent.class);
    }
}
```

### 4.3. Обратная совместимость

Существующие модули сохраняют работоспособность без модификаций.

```java
// Существующий механизм без изменений
@Configuration
@ComponentScan
@JmixModule(dependsOn = CoreConfiguration.class)
public class MyExistingModule {
    @Bean
    public ComponentRegistration myOldComponent() {
        return ComponentRegistrationBuilder
            .create(MyOldComponent.class)
            .withComponentLoader("myOld", MyOldLoader.class)
            .build();
    }
}

// Упрощённый API в рамках новой подсистемы
public class MyNewPlugin extends JmixPlugin {
    @Override
    public void onLoad(PluginContext ctx) {
        ctx.registerComponent("myNew", MyNewComponent.class);
    }
}

// Оба механизма функционируют одновременно
```

---

## 5. Формат `plugin.json`: от прототипа к Java-реализации

### 5.1. Формат, определённый прототипом

```json
{
  "id": "io.jmix.plugins.example",
  "name": "Example Plugin",
  "version": "1.0.0",
  "compatibility": { "jmix": ">=2.0.0 <3.0.0" },
  "dependencies": {
    "plugins": [
      { "id": "io.jmix.core", "version": "^2.0.0" }
    ]
  },
  "entrypoints": { "main": "io.jmix.plugins.example.ExamplePlugin" }
}
```

### 5.2. Чтение дескриптора в Java-реализации

```java
ObjectMapper mapper = new ObjectMapper();
PluginDescriptor descriptor = mapper.readValue(
    pluginJarEntry("META-INF/plugin.json"),
    PluginDescriptor.class
);

String pluginClass = descriptor.getEntrypoints().getMain();
Class<?> clazz = classLoader.loadClass(pluginClass);
JmixPlugin plugin = (JmixPlugin) clazz.getDeclaredConstructor().newInstance();
```

---

## 6. Пример проектируемого плагина

### 6.1. Структура проекта

```
advanced-grid-plugin/
├── build.gradle                   # конфигурация сборки
├── src/main/
│   ├── java/io/jmix/plugins/advancedgrid/
│   │   ├── AdvancedGridPlugin.java         # главный класс плагина
│   │   ├── components/
│   │   │   └── AdvancedGrid.java           # UI-компонент
│   │   └── services/
│   │       └── GridDataService.java
│   └── resources/
│       ├── META-INF/
│       │   └── plugin.json                 # дескриптор плагина
│       └── i18n/
│           └── messages.properties
└── src/test/
    └── java/io/jmix/plugins/advancedgrid/
        └── AdvancedGridPluginTest.java
```

### 6.2. Главный класс плагина

```java
package io.jmix.plugins.advancedgrid;

import io.jmix.core.plugin.JmixPlugin;
import io.jmix.core.plugin.PluginContext;
import io.jmix.flowui.sys.registration.ComponentRegistrationBuilder;

public class AdvancedGridPlugin extends JmixPlugin {

    @Override
    public void onLoad(PluginContext context) throws PluginException {
        context.getLogger().info("Advanced Grid Plugin loading...");

        context.getComponentRegistry().register(
            "advancedGrid",
            AdvancedGrid.class,
            AdvancedGridLoader.class
        );

        context.registerBean("advancedGrid_DataService", GridDataService.class);

        context.on("dataGrid:rendered", this::onGridRendered);

        context.getLogger().info("Advanced Grid Plugin loaded successfully");
    }

    @Override
    public void onUnload() throws PluginException {
        getContext().getLogger().info("Advanced Grid Plugin unloading...");
    }

    private void onGridRendered(GridEvent event) {
        getContext().getLogger().debug("Grid rendered: {}", event.getGridId());
    }
}
```

### 6.3. Дескриптор `plugin.json`

```json
{
  "$schema": "https://jmix.io/schemas/plugin-v1.json",
  "id": "io.jmix.plugins.advanced-grid",
  "name": "Advanced Grid Component",
  "version": "2.1.0",
  "description": "Data grid component with filtering, sorting and export support",

  "author": {
    "name": "John Developer",
    "email": "john@example.com"
  },

  "license": {
    "type": "Apache-2.0"
  },

  "compatibility": {
    "jmix": ">=2.0.0 <3.0.0",
    "java": ">=17",
    "spring-boot": ">=3.0.0"
  },

  "dependencies": {
    "plugins": [
      {
        "id": "io.jmix.flowui",
        "version": "^2.0.0"
      }
    ]
  },

  "components": [
    {
      "tag": "advancedGrid",
      "class": "io.jmix.plugins.advancedgrid.components.AdvancedGrid",
      "icon": "assets/grid-icon.svg"
    }
  ],

  "permissions": [
    "jmix.data.read",
    "jmix.ui.component.register"
  ],

  "entrypoints": {
    "main": "io.jmix.plugins.advancedgrid.AdvancedGridPlugin"
  },

  "marketplace": {
    "categories": ["UI Components", "Data Display"],
    "keywords": ["grid", "table", "datagrid", "export", "filter"],
    "icon": "assets/marketplace-icon.png",
    "screenshots": [
      "assets/screenshot1.png",
      "assets/screenshot2.png"
    ]
  }
}
```

### 6.4. Конфигурация сборки

```groovy
plugins {
    id 'java'
    id 'io.jmix' version '2.0.0'
    id 'io.jmix.plugin-sdk' version '1.0.0' // проектируемый Gradle-плагин
}

group = 'io.jmix.plugins'
version = '2.1.0'

jmixPlugin {
    pluginId = 'io.jmix.plugins.advanced-grid'
    pluginName = 'Advanced Grid Component'
    mainClass = 'io.jmix.plugins.advancedgrid.AdvancedGridPlugin'

    compatibility {
        jmix = '>=2.0.0 <3.0.0'
    }

    permissions = ['jmix.data.read', 'jmix.ui.component.register']
}

dependencies {
    implementation 'io.jmix.core:jmix-core'
    implementation 'io.jmix.flowui:jmix-flowui'
    implementation 'io.jmix.plugin:jmix-plugin-sdk:1.0.0'
}

tasks.register('packagePlugin', Jar) {
    dependsOn 'generatePluginDescriptor'

    archiveBaseName = jmixPlugin.pluginId
    archiveVersion = jmixPlugin.pluginVersion

    from(file("${buildDir}/generated/plugin.json")) {
        into 'META-INF'
    }
}
```

---

## 7. Перенос результатов прототипа в Java-реализацию

### 7.1. Использование прототипа как спецификации

Прототип определяет:

- формат `plugin.json`;
- интерфейсы API;
- обработчики жизненного цикла;
- структуру контекстного API;
- алгоритмы разрешения зависимостей.

Java-реализация воспроизводит указанные сущности с учётом особенностей интеграции с Spring Boot и обратной совместимости с `@JmixModule`.

### 7.2. Создание Java-пакета в `jmix-core`

```
jmix/jmix-core/core/src/main/java/io/jmix/core/
├── annotation/
│   └── JmixModule.java (существующий)
├── plugin/ (проектируемый)
│   ├── JmixPlugin.java
│   ├── PluginDescriptor.java
│   ├── JmixPluginManager.java
│   ├── PluginContext.java
│   ├── PluginException.java
│   ├── PluginState.java
│   └── PluginConfiguration.java
└── ...
```

### 7.3. Интеграция с `CoreConfiguration`

```java
// jmix/jmix-core/core/src/main/java/io/jmix/core/CoreConfiguration.java

@Configuration
@ComponentScan
@JmixModule(dependsOn = {})
@Import({CoreScheduleConfiguration.class, PluginConfiguration.class})
public class CoreConfiguration {
    // существующие beans, дополнительно подключается PluginConfiguration
}
```

### 7.4. Свойства приложения

```yaml
# application.yml
jmix:
  plugin:
    enabled: true
    locations:
      - classpath:plugins/
      - file:${jmix.core.work-dir}/plugins/
    auto-load: true
```

---

## 8. Сценарии использования в приложении

### 8.1. Применение менеджера плагинов

```java
@SpringBootApplication
@EnableJmix
public class MyApplication {

    @Autowired
    private JmixPluginManager pluginManager;

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        // автоматическая загрузка плагинов из jmix.plugin.locations;
        // допускается также явная загрузка

        pluginManager.loadPlugin(new File("plugins/my-plugin-1.0.0.jar"));

        List<PluginInfo> plugins = pluginManager.list();
        plugins.forEach(p ->
            System.out.println(p.getName() + " " + p.getVersion() + " - " + p.getState())
        );
    }
}
```

### 8.2. REST API управления плагинами

```java
@RestController
@RequestMapping("/api/plugins")
public class PluginRestController {

    @Autowired
    private JmixPluginManager pluginManager;

    @GetMapping
    public List<PluginInfo> listPlugins() {
        return pluginManager.list();
    }

    @PostMapping("/load")
    public void loadPlugin(@RequestParam String pluginId) {
        pluginManager.load(pluginId);
    }

    @PostMapping("/unload")
    public void unloadPlugin(@RequestParam String pluginId) {
        pluginManager.unload(pluginId);
    }

    @PostMapping("/upload")
    public void uploadPlugin(@RequestParam("file") MultipartFile file) {
        File tempFile = File.createTempFile("plugin", ".jar");
        file.transferTo(tempFile);
        pluginManager.loadPlugin(tempFile);
    }
}
```

---

## 9. Перечень задач Java-реализации

### 9.1. Ядро подсистемы плагинов

- сформировать пакет `io.jmix.core.plugin`;
- реализовать базовый класс `JmixPlugin`;
- реализовать `PluginDescriptor` с привязкой Jackson;
- реализовать `JmixPluginManager`;
- реализовать интерфейс `PluginContext`;
- определить `PluginConfiguration` с `@ConditionalOnProperty`;
- определить `PluginProperties` для интеграции с `application.yml`.

### 9.2. Изоляция загрузчиков классов

- реализовать `PluginClassLoader` с механизмом изоляции;
- реализовать делегирование parent-child;
- обеспечить обработку конфликтов версий библиотек;
- реализовать подсистему безопасности и песочницу.

### 9.3. Интеграция со Spring

- интегрировать с `JmixModulesProcessor`;
- сформировать `ApplicationContext`, специфичный для плагина;
- реализовать внедрение Spring-зависимостей в плагины;
- определить Spring-события жизненного цикла плагинов.

### 9.4. Регистрация компонентов

- интегрировать с `CustomComponentsRegistry`;
- упростить API регистрации компонентов;
- реализовать автоматическую генерацию `ComponentLoader`.

### 9.5. Инструменты разработчика

- разработать Gradle-плагин `io.jmix.plugin-sdk`;
- реализовать задачу `generatePluginDescriptor`;
- реализовать задачу `packagePlugin`;
- подготовить Maven-архетип для плагинов.

### 9.6. Тестирование

- определить аннотацию `@JmixPluginTest`;
- реализовать расширение `PluginTestExtension` для JUnit 5;
- подготовить mock-реализации;
- разработать вспомогательные утилиты тестирования.

---

## 10. План дальнейших работ

В рамках развития подсистемы расширяемости предусматриваются следующие этапы:

1. Спецификация (выполнено): TypeScript-прототип, проектирование API, документация, примеры.
2. Java-реализация ядра: подсистема плагинов, интеграция со Spring Boot, Gradle-плагин, модульные тесты.
3. Интеграция со Studio: пользовательский интерфейс управления плагинами, диалог установки, представление состояния, обозреватель маркетплейса.
4. Маркетплейс: серверная часть (Spring Boot, PostgreSQL), клиентская часть (React), процесс публикации, функции сообщества.

---

## 11. Часто задаваемые вопросы

### 11.1. Почему прототип реализован на TypeScript, если Jmix — фреймворк на Java?

Выбор TypeScript обусловлен задачей быстрой валидации архитектурных решений. Использование TypeScript позволило:

- сократить срок подготовки прототипа;
- упростить экспериментирование с проектируемым API;
- получить исполняемую документацию архитектуры;
- избежать модификации существующей кодовой базы Jmix на исследовательской стадии.

### 11.2. Возможно ли использование TypeScript-плагинов в Jmix непосредственно?

Прямое использование невозможно: Jmix построен на платформе Java/Spring Boot. Допустимы следующие подходы:

- интеграция через мост для JavaScript-плагинов (например, на основе GraalVM);
- использование Web Components для UI-компонентов;
- основная подсистема плагинов реализуется на Java.

### 11.3. Какова дальнейшая роль прототипа после Java-реализации?

Прототип сохраняется в следующих качествах:

- спецификация проектируемого API;
- документация архитектурных подходов;
- обучающий материал;
- референсная реализация для разработчиков плагинов.

### 11.4. Сроки готовности к производственному использованию

Производственное использование становится возможным после завершения Java-реализации. Поэтапная последовательность определена в разделе 10.

---

## 12. Связанные материалы

- Стратегия интеграции: [INTEGRATION_STRATEGY.md](integration.md);
- Архитектурный анализ: [research/architecture/01_jmix_architectural_analysis.md](../research/architecture/01_jmix_architectural_analysis.md);
- Рекомендации по проектированию подсистемы расширяемости: [research/best-practices/04_best_practices_guide.md](../research/best-practices/04_best_practices_guide.md);
- Научная статья по результатам исследования: [research/paper/scientific_research_paper.md](../research/paper/scientific_research_paper.md).

---

## 13. Выводы

| Компонент | Статус | Применение |
|-----------|--------|------------|
| TypeScript-прототип | реализован | спецификация и обучение |
| Формат `plugin.json` | определён | используется в Java-реализации |
| Проектируемый API | валидирован | подлежит переносу в Java |
| Архитектурные подходы | подтверждены прототипом | подлежат реализации в Java |
| Java-реализация | требуется разработка | условие производственного применения |
| Производственная готовность | не достигнута | определяется завершением Java-реализации |

Прототип выступает в роли валидированной спецификации проектируемой подсистемы. Дальнейший этап работ — Java-реализация и интеграция в кодовую базу Jmix.

---

**Следующий шаг:** реализация Java-пакета `io.jmix.core.plugin`.
