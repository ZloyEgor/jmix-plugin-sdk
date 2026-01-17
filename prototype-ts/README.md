# TypeScript-прототип Jmix Plugin SDK

Каталог содержит исходный TypeScript-прототип, выступающий спецификацией Java-реализации SDK. Прототип сохраняется в репозитории как референс архитектуры; рабочий код располагается в Java-модулях `sdk-core`, `sdk-spring`, `sdk-test`.

## Состав

- `core/` — TypeScript-исходники ядра прототипа (`Plugin`, `PluginManager`, `PluginContext`, `PluginDescriptor`, `Logger`).
- `examples/hello-world-plugin/` — TypeScript-вариант демонстрационного плагина.

## Сборка

```bash
cd prototype-ts/core
npm install
npm run build
```

Сборка прототипа на TypeScript не является обязательной для работы Java-реализации.
