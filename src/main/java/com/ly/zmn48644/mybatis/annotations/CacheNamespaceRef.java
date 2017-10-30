
package com.ly.zmn48644.mybatis.annotations;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CacheNamespaceRef {
    /**
     * A namespace type to reference a cache (the namespace name become a FQCN of specified type)
     */
    Class<?> value() default void.class;

    /**
     * A namespace name to reference a cache
     *
     * @since 3.4.2
     */
    String name() default "";
}
