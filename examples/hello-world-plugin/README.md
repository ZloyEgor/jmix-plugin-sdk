# Демонстрационный плагин Hello World

## 1. Назначение

Минимальный плагин, иллюстрирующий базовые возможности Jmix Plugin SDK. Используется в качестве учебного примера и референсной реализации.

## 2. Демонстрируемые возможности

- загрузка и выгрузка плагина в соответствии с жизненным циклом;
- использование сервисов контекста (журналирование, уведомления);
- обработка событий через шину сообщений;
- работа с конфигурацией;
- проверка разрешений во время выполнения.

## 3. Установка

```bash
npm install
npm run build
```

## 4. Использование

```typescript
import { PluginManager } from '@jmix/plugin-sdk-core';
import HelloWorldPlugin from './dist';
import descriptor from './plugin.json';

const manager = new PluginManager(platformContext);
const plugin = new HelloWorldPlugin();

await manager.register(plugin, descriptor);
await manager.load('io.jmix.examples.hello-world');
```

## 5. Конфигурация

```json
{
  "greeting": "Hello, World!"
}
```

## 6. Структура каталога

```
hello-world-plugin/
├── plugin.json          # дескриптор плагина
├── package.json
├── tsconfig.json
├── src/
│   └── index.ts         # реализация плагина
├── assets/
│   └── icon.png         # пиктограмма плагина
└── README.md
```

## 7. Источники

- Документация Jmix Plugin SDK: https://docs.jmix.io/plugin-sdk;
- Руководство по разработке плагинов: https://docs.jmix.io/plugin-sdk/development-guide;
- API-справочник: https://docs.jmix.io/plugin-sdk/api.
