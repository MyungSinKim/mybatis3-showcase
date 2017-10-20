
package com.ly.zmn48644.mybatis.logging;

/**
 * MyBatis 统一各种具体日志实现,上层模块使用此接口.
 *
 * 只定义了四种日志级别,其他日志实现中的日志级别划分都是不同的.
 */
public interface Log {

    //debug 级别是否启用
    boolean isDebugEnabled();

    //trace 级别是否启用
    boolean isTraceEnabled();

    //error 级别
    void error(String s, Throwable e);

    void error(String s);

    //debug级别
    void debug(String s);

    //trace级别
    void trace(String s);

    //警告级别
    void warn(String s);

}
