package org.maverick.devtools.worker.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZeebeWorker {

    /**
     * Job type to subscribe to.
     */
    String type();

    /**
     * Worker name. If empty, the method name or bean name will be used.
     */
    String name() default "";

    /**
     * Job timeout in milliseconds.
     */
    long timeout() default -1;

    /**
     * Max jobs active.
     */
    int maxJobsActive() default -1;

    /**
     * Fetch all variables.
     */
    boolean fetchAllVariables() default true;

    /**
     * Fetch specific variables.
     */
    String[] fetchVariables() default {};
}
