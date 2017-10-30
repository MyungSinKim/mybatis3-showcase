
package com.ly.zmn48644.mybatis.annotations;


import com.ly.zmn48644.mybatis.mapping.StatementType;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SelectKey {
    String[] statement();

    String keyProperty();

    String keyColumn() default "";

    boolean before();

    Class<?> resultType();

    StatementType statementType() default StatementType.PREPARED;
}
