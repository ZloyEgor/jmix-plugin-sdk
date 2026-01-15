# Плагин Advanced Grid. Пример спецификации

## 1. Статус

Настоящий артефакт является примером спецификации плагина и описывает целевую структуру компонента после выполнения Java-реализации Plugin SDK.

Текущее состояние:

- `plugin.json` — подготовленная спецификация дескриптора, пригодная для использования;
- `build.gradle.example` — пример конфигурации сборки;
- исходный код на Java — не разработан, запланирован после реализации Plugin SDK в кодовой базе Jmix.

---

## 2. Назначение

Плагин предоставляет расширенный компонент таблицы данных (data grid) для Jmix. Функциональные возможности компонента:

- многоколоночная фильтрация с поддержкой операторов сравнения;
- многоколоночная сортировка;
- виртуальная прокрутка и стандартная пагинация;
- экспорт в форматы XLSX, CSV, PDF;
- настройка тем оформления, стилей и форматтеров;
- виртуальный рендеринг для работы с большими наборами данных.

---

## 3. Структура каталога

```
advanced-grid-plugin/
├── plugin.json                    # спецификация дескриптора (подготовлено)
├── build.gradle.example           # пример конфигурации сборки (подготовлено)
├── README.md                      # настоящий документ
├── src/main/java/ (планируется)
│   └── io/jmix/plugins/advancedgrid/
│       ├── AdvancedGridPlugin.java           # главный класс плагина
│       ├── components/
│       │   ├── AdvancedGrid.java             # компонент таблицы
│       │   └── AdvancedGridLoader.java       # XML-загрузчик
│       ├── models/
│       │   ├── FilterConfig.java
│       │   └── ExportConfig.java
│       └── services/
│           ├── GridDataService.java
│           └── ExportService.java
└── src/main/resources/
    ├── META-INF/
    │   └── plugin.json (копия из корня)
    ├── i18n/
    │   └── messages.properties
    └── config/
        └── schema.json
```

---

## 4. Спецификация Java-реализации

### 4.1. Главный класс плагина

```java
package io.jmix.plugins.advancedgrid;

import io.jmix.core.plugin.JmixPlugin;
import io.jmix.core.plugin.PluginContext;
import io.jmix.core.plugin.PluginException;

/**
 * Advanced Grid Plugin for Jmix.
 * Provides data grid component with filtering, sorting, and export support.
 */
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
        context.registerBean("advancedGrid_ExportService", ExportService.class);

        Integer pageSize = getConfig("pageSize", 20);
        Boolean virtualScroll = getConfig("enableVirtualScroll", true);

        context.getLogger().info(
            "Advanced Grid configured: pageSize={}, virtualScroll={}",
            pageSize, virtualScroll
        );

        context.on("grid:dataLoaded", this::onDataLoaded);

        context.getLogger().info("Advanced Grid Plugin loaded successfully");
    }

    @Override
    public void onUnload() throws PluginException {
        getContext().getLogger().info("Advanced Grid Plugin unloading...");

        getContext().getEventBus().removeAllListeners("grid:dataLoaded");

        getContext().getLogger().info("Advanced Grid Plugin unloaded");
    }

    @Override
    public void onConfigChange(String key, Object value) throws PluginException {
        getContext().getLogger().info("Config changed: {} = {}", key, value);

        if ("pageSize".equals(key)) {
            getContext().emit("grid:configChanged", key, value);
        }
    }

    private void onDataLoaded(GridDataEvent event) {
        getContext().getLogger().debug(
            "Grid data loaded: {} rows",
            event.getRowCount()
        );
    }
}
```

### 4.2. Класс компонента таблицы

```java
package io.jmix.plugins.advancedgrid.components;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.flowui.component.grid.DataGrid;

/**
 * Advanced Grid Component extending standard Jmix DataGrid.
 */
public class AdvancedGrid extends DataGrid {

    private FilterConfig filterConfig;
    private ExportConfig exportConfig;

    public AdvancedGrid() {
        super();
        initAdvancedFeatures();
    }

    private void initAdvancedFeatures() {
        this.filterConfig = new FilterConfig();
        this.exportConfig = new ExportConfig();

        setPageSize(20);
        addAdvancedRenderers();
    }

    public void exportToExcel() {
        // реализация экспорта средствами Apache POI
    }

    public void exportToCsv() {
        // реализация экспорта в CSV
    }

    public void applyAdvancedFilter(FilterConfig config) {
        this.filterConfig = config;
        refreshData();
    }
}
```

### 4.3. Конфигурация приложения

```yaml
# application.yml
jmix:
  plugin:
    enabled: true
    locations:
      - file:${user.home}/.jmix/plugins/

plugins:
  io.jmix.plugins.advanced-grid:
    pageSize: 25
    enableVirtualScroll: true
    enableExport: true
    exportFormats:
      - xlsx
      - csv
      - pdf
    theme: dark
```

---

## 5. Сценарии установки

Описанные ниже сценарии относятся к проектируемой инфраструктуре и предполагают завершение Java-реализации Plugin SDK.

### 5.1. Через инструмент командной строки

```bash
jmix plugin install advanced-grid
jmix plugin install advanced-grid-2.1.0.jar
```

### 5.2. Через Gradle

```groovy
dependencies {
    jmixPlugin 'io.jmix.plugins:advanced-grid:2.1.0'
}
```

### 5.3. Через интерфейс Jmix Studio

1. открыть Jmix Studio;
2. перейти в раздел «Plugins → Browse Marketplace»;
3. выбрать «Advanced Grid»;
4. выполнить установку;
5. перезапустить приложение.

---

## 6. Использование

### 6.1. XML-дескриптор представления

```xml
<view xmlns="http://jmix.io/schema/flowui/view"
      xmlns:grid="http://jmix.io/schema/plugins/advanced-grid">

    <data>
        <collection id="usersDc" class="User">
            <fetchPlan extends="_base"/>
        </collection>
    </data>

    <layout>
        <grid:advancedGrid id="usersGrid"
                          dataContainer="usersDc"
                          pageSize="25"
                          enableExport="true"
                          enableVirtualScroll="true">

            <grid:columns>
                <grid:column property="username" sortable="true" filterable="true"/>
                <grid:column property="email" sortable="true" filterable="true"/>
                <grid:column property="active" sortable="true" filterable="true"/>
            </grid:columns>

            <grid:export>
                <grid:format type="xlsx" fileName="users.xlsx"/>
                <grid:format type="csv" fileName="users.csv"/>
                <grid:format type="pdf" fileName="users.pdf"/>
            </grid:export>

        </grid:advancedGrid>
    </layout>
</view>
```

### 6.2. Контроллер представления

```java
@ViewController("UsersView")
@ViewDescriptor("users-view.xml")
public class UsersView extends StandardView {

    @ViewComponent
    private AdvancedGrid usersGrid; // тип, экспортируемый плагином

    @Subscribe
    public void onInit(InitEvent event) {
        usersGrid.applyAdvancedFilter(
            FilterConfig.builder()
                .addTextFilter("username", FilterOperator.CONTAINS)
                .addBooleanFilter("active", true)
                .build()
        );
    }

    @Subscribe("exportButton")
    public void onExportClick(ClickEvent<Button> event) {
        usersGrid.exportToExcel();
    }
}
```

---

## 7. Параметры плагина

Параметры плагина задаются в файле `application.yml`.

```yaml
plugins:
  io.jmix.plugins.advanced-grid:
    pageSize: 25

    enableVirtualScroll: true

    enableExport: true
    exportFormats: [xlsx, csv, pdf]

    advancedFiltering:
      enabled: true
      operators: [equals, contains, startsWith, endsWith, greaterThan, lessThan]

    performance:
      cacheResults: true
      cacheTtl: 300
      maxCachedQueries: 100

    ui:
      theme: light
      density: comfortable
      showRowNumbers: true
      highlightOnHover: true
```

---

## 8. Зависимости

Плагин использует следующие компоненты:

- Jmix Core (^2.0.0) — доступ к данным;
- Jmix FlowUI (^2.0.0) — UI-компоненты;
- Apache POI (5.2.5) — экспорт в формат Excel;
- OpenPDF (1.3.30) — экспорт в формат PDF.

Все зависимости объявляются в `plugin.json` и разрешаются автоматически менеджером плагинов.

---

## 9. Сценарий разработки

### 9.1. Создание заготовки плагина

```bash
jmix plugin init advanced-grid --type component

cd advanced-grid

jmix plugin dev

./gradlew packagePlugin

./gradlew installLocal
```

### 9.2. Тестирование

```java
@JmixPluginTest
@TestPlugin("advanced-grid-2.1.0.jar")
class AdvancedGridPluginTest {

    @Autowired
    private JmixPluginManager pluginManager;

    @Test
    void testPluginLoads() {
        assertTrue(pluginManager.isLoaded("io.jmix.plugins.advanced-grid"));
    }

    @Test
    void testGridComponentRegistered() {
        JmixPlugin plugin = pluginManager.get("io.jmix.plugins.advanced-grid");
        assertNotNull(plugin);

        ComponentRegistry registry = plugin.getContext().getComponentRegistry();
        assertTrue(registry.has("advancedGrid"));
    }
}
```

---

## 10. Публикация

Публикация в маркетплейс относится к проектируемой инфраструктуре. Предполагаемая последовательность шагов:

1. валидация дескриптора `plugin.json`;
2. проверка требований безопасности;
3. статический анализ исходного кода;
4. выполнение модульных и интеграционных тестов;
5. загрузка артефакта в репозиторий;
6. автоматизированное рецензирование;
7. публикация.

---

## 11. Лицензия

Apache 2.0.

---

## 12. Источники

- Документация плагина: https://docs.jmix-plugins.io/advanced-grid;
- Репозиторий: https://github.com/jmix-plugins/advanced-grid.

---

Примечание. Документ описывает целевую структуру плагина. Java-реализация подлежит разработке после завершения Plugin SDK в кодовой базе Jmix.
