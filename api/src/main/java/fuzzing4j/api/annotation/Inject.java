package fuzzing4j.api.annotation;

import fuzzing4j.api.Configurator;
import fuzzing4j.api.NONE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:01
 * @description
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
    Class<? extends Configurator> cfg()default NONE.class;
    String value()default "";
}
