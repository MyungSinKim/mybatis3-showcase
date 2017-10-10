
package com.ly.zmn48644.mybatis.logging;

/**
 * MyBatis 统一各种具体日志实现,上层模块使用此接口.
 */
public interface Log {


    boolean isDebugEnabled();

    boolean isTraceEnabled();

    void error(String s, Throwable e);

    void error(String s);

    void debug(String s);

    void trace(String s);

    void warn(String s);

}
