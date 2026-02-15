package io.jmix.plugin.test;

import io.jmix.plugin.core.JmixPluginManager;
import io.jmix.plugin.core.PluginContext;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.annotation.Annotation;

/**
 * JUnit 5 extension that supplies {@link PluginContext} and
 * {@link JmixPluginManager} parameters to test methods. The extension
 * is activated automatically by {@link JmixPluginTest}.
 */
public final class PluginTestExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(PluginTestExtension.class);

    private static final String CONTEXT_KEY = "context";
    private static final String MANAGER_KEY = "manager";

    @Override
    public void beforeEach(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);
        Settings settings = readSettings(context);
        MockPluginContext pluginContext = MockPluginContext.builder(settings.pluginId)
                .platformVersion(settings.platformVersion)
                .build();
        store.put(CONTEXT_KEY, pluginContext);
        store.put(MANAGER_KEY, new TestPluginManager(settings.platformVersion, pluginContext));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        context.getStore(NAMESPACE).remove(CONTEXT_KEY);
        context.getStore(NAMESPACE).remove(MANAGER_KEY);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        return PluginContext.class.isAssignableFrom(type)
                || MockPluginContext.class.isAssignableFrom(type)
                || JmixPluginManager.class.isAssignableFrom(type)
                || TestPluginManager.class.isAssignableFrom(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        ExtensionContext.Store store = extensionContext.getStore(NAMESPACE);
        if (MockPluginContext.class.isAssignableFrom(type) || PluginContext.class.isAssignableFrom(type)) {
            return store.get(CONTEXT_KEY);
        }
        if (TestPluginManager.class.isAssignableFrom(type) || JmixPluginManager.class.isAssignableFrom(type)) {
            return store.get(MANAGER_KEY);
        }
        throw new ParameterResolutionException("Unsupported parameter type: " + type);
    }

    private Settings readSettings(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        Annotation annotation = testClass.getAnnotation(JmixPluginTest.class);
        if (annotation == null) {
            return new Settings("test-plugin", "2.0.0");
        }
        JmixPluginTest spec = (JmixPluginTest) annotation;
        return new Settings(spec.pluginId(), spec.platformVersion());
    }

    private record Settings(String pluginId, String platformVersion) { }
}
