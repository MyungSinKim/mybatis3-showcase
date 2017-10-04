
package com.ly.zmn48644.mybatis.type;

import java.lang.annotation.*;

 //@UsesJava8
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MappedJdbcTypes {
  JdbcType[] value();
  boolean includeNullJdbcType() default false;
}
