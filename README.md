# Jmix Plugin SDK

## Прототипная реализация механизма расширяемости

| Параметр | Значение |
|----------|----------|
| Версия | 1.0.0-alpha |
| Статус | TypeScript-прототип + Java-реализация (sdk-core, sdk-spring, sdk-test) |
| Последнее обновление | 27 февраля 2026 г. |

---

## Замечание о статусе прототипа

Репозиторий содержит две согласованные ветви реализации:

1. **TypeScript-прототип** — расположен в подкаталоге `prototype-ts/`. Является спецификацией архитектуры и интерфейсов SDK. Не интегрируется с Java-кодовой базой Jmix.
2. **Java-реализация** — модули `sdk-core/`, `sdk-spring/`, `sdk-test/` и Java-версии примеров `examples/hello-world-plugin/`, `examples/advanced-grid-plugin/`. Реализация переносит API прототипа на Java 17 и Spring Boot 3, обеспечивает изоляцию через собственный `URLClassLoader`, предоставляет REST API управления плагинами и испытательный стенд на базе JUnit 5.

TypeScript-прототип:

- демонстрирует проектируемый интерфейс и архитектуру SDK;
- иллюстрирует жизненный цикл плагина;
- валидирует формат дескриптора;
- содержит примеры использования;
- не интегрирован с Java-кодовой базой Jmix.

Java-реализация:

- предоставляет `JmixPlugin`, `JmixPluginManager`, `PluginContext` и сервисные интерфейсы в модуле `sdk-core`;
- содержит автоконфигурацию Spring Boot и REST-контроллер `/api/plugins` в модуле `sdk-spring`;
- предлагает аннотацию `@JmixPluginTest` и `MockPluginContext` для unit-тестов в модуле `sdk-test`;
- использует изолированный `PluginClassLoader` и поддерживает регистрацию плагинов из JAR-файлов;
- покрыта модульными тестами; команда `./gradlew build` выполняется без ошибок.

Для полноценной интеграции с Jmix Studio и `JmixModulesProcessor` потребуется дополнительный связующий модуль; объём текущей реализации соответствует SDK-уровню и не затрагивает внутреннюю инфраструктуру Jmix.

## Быстрый старт (Java)

```bash
./gradlew :sdk-core:build
./gradlew :examples:hello-world-plugin:build
./gradlew build      # сборка всех модулей и запуск тестов
```

Подключение SDK в потребительский проект:

```groovy
dependencies {
    implementation 'io.jmix.plugin:sdk-core:1.0.0-alpha.1'
    implementation 'io.jmix.plugin:sdk-spring:1.0.0-alpha.1'
    testImplementation 'io.jmix.plugin:sdk-test:1.0.0-alpha.1'
}
```

При наличии `sdk-spring` Spring Boot автоматически активирует `PluginAutoConfiguration` и зарегистрирует `JmixPluginManager` в `ApplicationContext`.

---

## Содержание

1. Назначение и область применения
2. Архитектура SDK
3. Установка
4. Сценарий использования
5. Формат дескриптора `plugin.json`
6. Разработка пользовательских компонентов
7. Жизненный цикл и обработчики событий
8. Платформенные API
9. Тестирование
10. Команды CLI (проектируемая часть)
11. Конфигурация плагинов
12. Модель безопасности
13. Производительность и ограничения ресурсов
14. Примеры
15. Сценарий миграции существующих модулей
16. Интеграция с фреймворком Jmix
17. План дальнейших работ

---

## 1. Назначение и область применения

Jmix Plugin SDK — инструментарий для разработки, тестирования и распространения расширений фреймворка Jmix. Прототип демонстрирует подходы к расширяемости, выявленные в результате анализа распространённых low-code платформ.

Возможности SDK:

- формат дескриптора плагина в виде JSON-документа;
- менеджер плагинов с управлением жизненным циклом и разрешением зависимостей;
- интерфейс для разработки UI-компонентов на React;
- инструмент командной строки для скаффолдинга, разработки и публикации (проектируемая часть);
- утилиты модульного и интеграционного тестирования;
- модель безопасности на основе разрешений;
- встроенные средства профилирования и сбора метрик.

---

## 2. Архитектура SDK

Логическая структура SDK представлена ниже. Подсистема `cli/` и часть платформенных API в Java-модулях помечены как проектируемые и в текущей версии репозитория не реализованы.

```
jmix-plugin-sdk/
├── settings.gradle, build.gradle, gradle.properties
├── gradlew, gradlew.bat, gradle/wrapper/...
├── README.md, integration.md, prototype.md
├── LICENSE, .gitignore
├── prototype-ts/                  # TypeScript-прототип (спецификация)
│   ├── core/                      # классы Plugin, PluginManager, PluginContext
│   └── examples/hello-world-plugin/
├── sdk-core/                      # Java-ядро SDK (io.jmix.plugin.core)
│   ├── build.gradle
│   └── src/main/java/...          # JmixPlugin, JmixPluginManager,
│                                  # PluginContext, PluginDescriptor,
│                                  # PluginClassLoader, JarPluginLoader
├── sdk-spring/                    # Spring Boot интеграция и REST API
│   ├── build.gradle
│   └── src/main/java/...          # PluginAutoConfiguration,
│                                  # SpringPluginContext, PluginRestController
├── sdk-test/                      # JUnit 5 тестовый каркас
│   ├── build.gradle
│   └── src/main/java/...          # @JmixPluginTest, MockPluginContext,
│                                  # TestPluginManager
├── examples/                      # примеры плагинов (Java + plugin.json)
│   ├── hello-world-plugin/
│   └── advanced-grid-plugin/
└── cli/                           # инструмент командной строки (проектируемая часть)
```

---

## 3. Установка

Установка ядра SDK:

```bash
npm install @jmix/plugin-sdk
```

Глобальная установка инструмента командной строки (предполагаемый артефакт):

```bash
npm install -g @jmix/plugin-sdk-cli
```

---

## 4. Сценарий использования

Ниже приведён типовой сценарий разработки плагина с использованием проектируемого CLI и API ядра.

### 4.1. Создание заготовки плагина

```bash
jmix plugin init my-plugin
cd my-plugin
npm install
```

### 4.2. Реализация плагина

```typescript
// src/index.ts
import { Plugin, PluginContext } from '@jmix/plugin-sdk';

export class MyPlugin extends Plugin {
    async onLoad(context: PluginContext): Promise<void> {
        context.componentRegistry.register(
            'my-component',
            MyComponent
        );
    }

    async onUnload(): Promise<void> {
    }
}
```

### 4.3. Запуск среды разработки

```bash
npm run dev
```

### 4.4. Сборка артефакта

```bash
npm run build
```

### 4.5. Публикация

```bash
jmix plugin publish
```

---

## 5. Формат дескриптора `plugin.json`

Каждый плагин содержит дескриптор `plugin.json`, описывающий метаданные, совместимость, зависимости и точки входа.

```json
{
  "$schema": "https://jmix.io/schemas/plugin-v1.json",
  "id": "io.jmix.plugins.my-plugin",
  "name": "My Plugin",
  "version": "1.0.0",
  "description": "Описание плагина",

  "author": {
    "name": "John Doe",
    "email": "john@example.com",
    "url": "https://example.com"
  },

  "license": {
    "type": "MIT",
    "url": "https://opensource.org/licenses/MIT"
  },

  "compatibility": {
    "jmix": ">=2.0.0 <3.0.0",
    "java": ">=17",
    "spring-boot": ">=3.0.0"
  },

  "dependencies": {
    "plugins": [
      {
        "id": "io.jmix.core.ui",
        "version": "^2.0.0"
      }
    ]
  },

  "components": [
    {
      "tag": "my-component",
      "class": "io.jmix.plugins.MyComponent"
    }
  ],

  "permissions": [
    "jmix.data.read",
    "jmix.ui.component.register"
  ],

  "entrypoints": {
    "main": "dist/index.js",
    "config": "config/schema.json"
  }
}
```

---

## 6. Разработка пользовательских компонентов

Пример пользовательского компонента, реализованного на React и зарегистрированного через декораторы SDK.

```typescript
import React from 'react';
import { Component, Property } from '@jmix/plugin-sdk';

@Component({
    tag: 'custom-slider',
    displayName: 'Custom Slider',
    category: 'Input',
    icon: 'slider.svg'
})
export class CustomSlider extends React.Component<CustomSliderProps> {
    @Property({
        type: 'number',
        displayName: 'Value',
        required: true
    })
    value: number;

    @Property({
        type: 'number',
        displayName: 'Minimum',
        default: 0
    })
    min: number = 0;

    @Property({
        type: 'number',
        displayName: 'Maximum',
        default: 100
    })
    max: number = 100;

    render() {
        return (
            <input
                type="range"
                value={this.props.value}
                min={this.props.min}
                max={this.props.max}
                onChange={(e) => this.props.onValueChange(parseInt(e.target.value))}
                className="jmix-slider"
            />
        );
    }
}
```

---

## 7. Жизненный цикл и обработчики событий

SDK предоставляет обработчики жизненного цикла сущностей и общие обработчики ошибок.

```typescript
import { Plugin, PluginContext, EntityHooks } from '@jmix/plugin-sdk';

export class MyPlugin extends Plugin implements EntityHooks {
    async beforeEntityCreate(entity: any, context: PluginContext): Promise<any> {
        if (!entity.validatedBy) {
            entity.validatedBy = context.currentUser.id;
        }
        return entity;
    }

    async afterEntityCreate(entity: any, context: PluginContext): Promise<void> {
        await context.notificationService.notify({
            message: `Entity ${entity.id} created`,
            type: 'success'
        });
    }

    async onError(error: Error, context: PluginContext): Promise<void> {
        context.logger.error('Plugin error:', error);
    }
}
```

---

## 8. Платформенные API

Контекст плагина предоставляет доступ к сервисам данных, безопасности, пользовательского интерфейса, журналирования, конфигурации, локального хранилища и шины событий.

```typescript
import { PluginContext } from '@jmix/plugin-sdk';

export class MyPlugin extends Plugin {
    async onLoad(context: PluginContext): Promise<void> {
        const users = await context.dataService.query('User', {
            filter: { active: true },
            limit: 10
        });

        if (context.securityService.hasPermission('admin')) {
            // ветка для администратора
        }

        context.uiService.showNotification({
            message: 'Plugin loaded successfully',
            type: 'info'
        });

        context.logger.info('Plugin initialized');

        const apiKey = context.getConfig<string>('apiKey');

        await context.localStorage.set('lastSync', new Date().toISOString());

        context.eventBus.on('entity:created', (entity) => {
            context.logger.debug('Entity created', entity);
        });
    }
}
```

---

## 9. Тестирование

### 9.1. Модульное тестирование

```typescript
import { describe, it, expect } from '@jmix/plugin-sdk/testing';
import { MyPlugin } from '../src';
import { createMockContext } from '@jmix/plugin-sdk/testing';

describe('MyPlugin', () => {
    it('should load successfully', async () => {
        const plugin = new MyPlugin();
        const context = createMockContext();

        await plugin.onLoad(context);

        expect(context.componentRegistry.has('my-component')).toBe(true);
    });

    it('should handle entity creation', async () => {
        const plugin = new MyPlugin();
        const context = createMockContext();
        const entity = { name: 'Test' };

        const result = await plugin.beforeEntityCreate(entity, context);

        expect(result.validatedBy).toBeDefined();
    });
});
```

### 9.2. Интеграционное тестирование

```typescript
import { IntegrationTestRunner } from '@jmix/plugin-sdk/testing';

const runner = new IntegrationTestRunner({
    plugins: ['my-plugin'],
    mockData: {
        users: [
            { id: 1, name: 'John Doe' }
        ]
    }
});

describe('Integration Tests', () => {
    it('should integrate with Jmix', async () => {
        await runner.start();

        const component = runner.getComponent('my-component');
        expect(component).toBeDefined();

        await runner.stop();
    });
});
```

---

## 10. Команды CLI (проектируемая часть)

Указанные команды описывают предполагаемый интерфейс инструмента командной строки и в текущей версии прототипа не реализованы.

### 10.1. Инициализация заготовки

```bash
jmix plugin init <name> [options]

Options:
  --type <type>        Plugin type (component|module|integration)
  --template <name>    Use template (basic|advanced|minimal)
  --typescript         Use TypeScript (default: true)
  --react              Use React for components
```

### 10.2. Сервер разработки

```bash
jmix plugin dev [options]

Options:
  --port <port>        Dev server port (default: 3000)
  --hot-reload         Enable hot reload (default: true)
  --open               Open browser automatically
```

### 10.3. Сборка

```bash
jmix plugin build [options]

Options:
  --mode <mode>        Build mode (development|production)
  --watch              Watch for changes
  --minify             Minify output
```

### 10.4. Валидация дескриптора

```bash
jmix plugin validate [options]

Options:
  --strict             Strict validation mode
  --fix                Auto-fix issues when possible
```

### 10.5. Публикация

```bash
jmix plugin publish [options]

Options:
  --registry <url>     Marketplace URL
  --token <token>      Authentication token
  --dry-run            Validate without publishing
```

---

## 11. Конфигурация плагинов

Конфигурация плагина описывается JSON-схемой и валидируется средствами SDK.

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "apiKey": {
      "type": "string",
      "description": "API key for external service",
      "minLength": 32
    },
    "endpoint": {
      "type": "string",
      "format": "uri",
      "default": "https://api.example.com"
    },
    "timeout": {
      "type": "number",
      "minimum": 1000,
      "maximum": 30000,
      "default": 5000
    },
    "features": {
      "type": "object",
      "properties": {
        "enableCache": {
          "type": "boolean",
          "default": true
        },
        "maxRetries": {
          "type": "integer",
          "minimum": 0,
          "maximum": 5,
          "default": 3
        }
      }
    }
  },
  "required": ["apiKey"]
}
```

---

## 12. Модель безопасности

### 12.1. Подсистема разрешений

Разрешения объявляются в дескрипторе и проверяются во время выполнения.

```typescript
// Declare permissions in plugin.json
{
  "permissions": [
    {
      "id": "data.read",
      "description": "Read application data",
      "scope": "entity:*",
      "required": true
    },
    {
      "id": "network.http",
      "description": "Make HTTP requests",
      "scope": "https://*.example.com/*",
      "dangerous": true
    }
  ]
}

if (context.hasPermission('network.http')) {
    await fetch('https://api.example.com/data');
}
```

### 12.2. Изоляция плагинов

Плагины исполняются в изолированной среде:

- отдельные загрузчики классов для Java-реализации;
- Web Workers для JavaScript-реализации;
- ограничения на ресурсы, налагаемые средой исполнения;
- контролируемый сетевой доступ.

---

## 13. Производительность и ограничения ресурсов

### 13.1. Лимиты ресурсов

```json
{
  "resourceLimits": {
    "maxMemory": "50MB",
    "maxCpu": "10%",
    "maxStorage": "100MB",
    "maxNetworkRequests": 100,
    "rateLimit": "10 req/second"
  }
}
```

### 13.2. Сбор метрик

```typescript
context.metrics.record('operation:duration', duration);
context.metrics.increment('operation:count');

const metrics = await context.metrics.getReport();
context.logger.info('Avg response time', metrics.avg('operation:duration'));
```

---

## 14. Примеры

В каталоге `examples/` размещены следующие плагины:

- `hello-world-plugin` — минимальный плагин, демонстрирующий жизненный цикл и использование контекстных сервисов;
- `advanced-grid-plugin` — спецификация плагина, реализующего расширенный компонент таблицы данных с фильтрацией, сортировкой, виртуальной прокруткой и экспортом.

---

## 15. Сценарий миграции существующих модулей

Раздел описывает проектируемый сценарий преобразования модуля `@JmixModule` в плагин SDK. Команды относятся к проектируемому CLI и не реализованы в текущей версии прототипа.

### 15.1. Создание заготовки

```bash
jmix plugin init my-module --type module
```

### 15.2. Перенос исходного кода

```bash
cp -r src/main/java/com/example/mymodule/* jmix-plugin/src/
cp -r src/main/resources/* jmix-plugin/resources/
```

### 15.3. Генерация дескриптора

```bash
jmix plugin validate --generate-descriptor
```

### 15.4. Обновление зависимостей

```bash
jmix plugin migrate --from jmix-legacy --to plugin-sdk
```

### 15.5. Сборка и тестирование

```bash
npm test
npm run build
```

---

## 16. Интеграция с фреймворком Jmix

### 16.1. Архитектурная стратегия

Подсистема плагинов проектируется как дополнение к существующему механизму `@JmixModule`. Предполагается двухуровневая архитектура расширяемости.

```
Архитектура расширяемости Jmix
├── Уровень 1. Статические модули (существующий механизм)
│   ├── аннотация @JmixModule
│   ├── разрешение зависимостей на этапе компиляции
│   ├── Spring-конфигурации (@Configuration)
│   └── используется ядром фреймворка и существующими расширениями
│
└── Уровень 2. Динамические плагины (проектируемый механизм)
    ├── аннотация @JmixPlugin
    ├── загрузка из JAR-файлов во время выполнения
    ├── упрощённый API на базе класса Plugin
    └── используется сторонними расширениями
```

### 16.2. Контур Java-реализации

Для производственной интеграции предполагается реализация следующих сущностей.

```java
// jmix-core/core/src/main/java/io/jmix/core/plugin/JmixPlugin.java
package io.jmix.core.plugin;

import org.springframework.context.ApplicationContext;

public abstract class JmixPlugin {
    private PluginDescriptor descriptor;
    private PluginContext context;

    public abstract void onLoad(PluginContext context) throws PluginException;
    public abstract void onUnload() throws PluginException;

    protected <T> T getBean(Class<T> beanClass) {
        return context.getApplicationContext().getBean(beanClass);
    }
}

@Configuration
@ConditionalOnProperty(name = "jmix.plugin.enabled", havingValue = "true")
public class PluginConfiguration {

    @Bean("core_PluginManager")
    public JmixPluginManager pluginManager(
            ApplicationContext applicationContext,
            JmixModules jmixModules) {
        return new JmixPluginManager(applicationContext, jmixModules);
    }
}
```

### 16.3. Обратная совместимость

Существующие модули `@JmixModule` сохраняют работоспособность без модификаций.

```java
// Существующий механизм продолжает работать
@Configuration
@JmixModule(dependsOn = CoreConfiguration.class)
public class MyModuleConfiguration {
    @Bean
    public ComponentRegistration myComponent() {
        return ComponentRegistrationBuilder.create(MyComponent.class)
            .withComponentLoader("myComponent", MyComponentLoader.class)
            .build();
    }
}

// Проектируемый упрощённый API
public class MyPlugin extends JmixPlugin {
    @Override
    public void onLoad(PluginContext context) {
        context.registerComponent("myComponent", MyComponent.class);
    }
}
```

### 16.4. Установка плагинов

Предполагаемые сценарии установки:

```bash
# Через CLI (проектируемая часть)
jmix plugin install advanced-grid-2.1.0.jar

# Через файл конфигурации
# application.properties
jmix.plugin.locations=file:/opt/jmix/plugins/
jmix.plugin.enabled=true

# Через интерфейс Jmix Studio (проектируемая часть)
# [Plugins] -> [Install] -> Browse JAR
```

### 16.5. Структура JAR-артефакта плагина

```
my-plugin.jar
├── META-INF/
│   ├── MANIFEST.MF
│   └── plugin.json          # формат дескриптора, определённый прототипом
├── io/jmix/plugins/myplugin/
│   ├── MyPlugin.class       # наследник JmixPlugin
│   └── components/
│       └── MyComponent.class
└── resources/
    └── i18n/messages.properties
```

Формат `plugin.json`, определённый в TypeScript-прототипе, используется в качестве спецификации для Java-реализации.

---

## 17. План дальнейших работ

В рамках развития SDK предполагается выполнение следующих этапов:

- завершение формата дескриптора, ядра менеджера плагинов и SDK для компонентов (выполнено в рамках прототипа);
- реализация серверной части маркетплейса плагинов;
- разработка визуального конструктора компонентов;
- оптимизация горячей перезагрузки;
- завершение фреймворка тестирования;
- разработка клиентской части маркетплейса;
- расширение модели безопасности;
- оптимизация подсистемы выполнения;
- подготовка сайта документации;
- разработка интеграций с IDE;
- формирование библиотеки шаблонов;
- внедрение функциональности корпоративного уровня.

---

## Источники

- Документация Jmix: https://docs.jmix.io
- Репозиторий фреймворка: https://github.com/jmix-framework
- Спецификация формата дескриптора: настоящий документ, раздел 5.

---

Лицензия: Apache 2.0 (см. файл `LICENSE`).
