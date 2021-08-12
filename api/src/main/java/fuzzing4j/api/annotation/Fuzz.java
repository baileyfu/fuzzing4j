package fuzzing4j.api.annotation;

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
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Fuzz {
    int times()default 0;
    String duration()default "";
}
