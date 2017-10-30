
package com.ly.zmn48644.mybatis.annotations;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Results {
    /**
     * The name of the result map.
     */
    String id() default "";

    Result[] value() default {};
}
