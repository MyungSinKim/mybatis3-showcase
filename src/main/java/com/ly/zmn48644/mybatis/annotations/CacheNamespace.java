
package com.ly.zmn48644.mybatis.annotations;


import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CacheNamespace {

    //TODO 涉及到缓存模块临时注释

//  Class<? extends Cache> implementation() default PerpetualCache.class;
//
//  Class<? extends org.apache.ibatis.cache.Cache> eviction() default LruCache.class;
//
//  long flushInterval() default 0;
//
//  int size() default 1024;
//
//  boolean readWrite() default true;
//
//  boolean blocking() default false;
//
//  /**
//   * Property values for a implementation object.
//   * @since 3.4.2
//   */
//  Property[] properties() default {};

}
