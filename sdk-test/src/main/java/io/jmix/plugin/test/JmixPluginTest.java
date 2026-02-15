package io.jmix.plugin.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation marking a JUnit 5 test class as a Jmix plugin test.
 * Activates {@link PluginTestExtension}, providing {@link PluginContext}
 * and {@link io.jmix.plugin.core.JmixPluginManager} parameter resolution.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ExtendWith(PluginTestExtension.class)
public @interface JmixPluginTest {

    /** Identifier used by the default {@link MockPluginContext}. */
    String pluginId() default "test-plugin";

    /** Reported platform version used by the test plugin manager. */
    String platformVersion() default "2.0.0";
}
