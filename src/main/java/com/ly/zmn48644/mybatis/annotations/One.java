
package com.ly.zmn48644.mybatis.annotations;


import com.ly.zmn48644.mybatis.mapping.FetchType;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface One {
    String select() default "";

    FetchType fetchType() default FetchType.DEFAULT;

}
