
package com.ly.zmn48644.mybatis.exceptions;


import com.ly.zmn48644.mybatis.executor.ErrorContext;

/**
 * 异常工厂类
 */
public class ExceptionFactory {

    private ExceptionFactory() {

    }

    public static RuntimeException wrapException(String message, Exception e) {
        //本地线程异常,的管理,这个还不太清楚
        return new PersistenceException(ErrorContext.instance().message(message).cause(e).toString(), e);
    }

}
