
package com.ly.zmn48644.mybatis.annotations;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SelectProvider {
  Class<?> type();

  String method();
}
