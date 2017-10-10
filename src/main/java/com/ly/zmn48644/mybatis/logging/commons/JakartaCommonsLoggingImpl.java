
package com.ly.zmn48644.mybatis.logging.commons;


import com.ly.zmn48644.mybatis.logging.Log;
import com.ly.zmn48644.mybatis.logging.LogFactory;

public class JakartaCommonsLoggingImpl implements Log {

    private final Log log;

    public JakartaCommonsLoggingImpl(String clazz) {
        log = LogFactory.getLog(clazz);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void error(String s, Throwable e) {
        log.error(s, e);
    }

    @Override
    public void error(String s) {
        log.error(s);
    }

    @Override
    public void debug(String s) {
        log.debug(s);
    }

    @Override
    public void trace(String s) {
        log.trace(s);
    }

    @Override
    public void warn(String s) {
        log.warn(s);
    }

}
