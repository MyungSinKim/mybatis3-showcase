
package com.ly.zmn48644.mybatis.logging;

import java.lang.reflect.Constructor;

public final class LogFactory {

    /**
     * Marker to be used by logging implementations that support markers
     * 如果使用的日志实现支持markers,属性将会被使用
     */
    public static final String MARKER = "MYBATIS";

    //指向 被选用的日志实现的 适配器构造方法
    private static Constructor<? extends Log> logConstructor;

    //针对不同的日志实现进行尝试加载.
    static {
        //首先尝试加载  slf4j
        tryImplementation(new Runnable() {
            @Override
            public void run() {
                useSlf4jLogging();
            }
        });

        tryImplementation(new Runnable() {
            @Override
            public void run() {
                useCommonsLogging();
            }
        });
        tryImplementation(new Runnable() {
            @Override
            public void run() {
                useLog4J2Logging();
            }
        });
        tryImplementation(new Runnable() {
            @Override
            public void run() {
                useLog4JLogging();
            }
        });
        tryImplementation(new Runnable() {
            @Override
            public void run() {
                useJdkLogging();
            }
        });

        //如果没有加载到任何日志实现, 提供了默认不做任何处理的日志实现.
        tryImplementation(new Runnable() {
            @Override
            public void run() {
                useNoLogging();
            }
        });
    }

    private LogFactory() {
        // disable construction
    }

    public static Log getLog(Class<?> aClass) {
        return getLog(aClass.getName());
    }

    public static Log getLog(String logger) {
        try {
            //调用构造方法
            return logConstructor.newInstance(logger);
        } catch (Throwable t) {
            throw new LogException("Error creating logger for logger " + logger + ".  Cause: " + t, t);
        }
    }


    /**
     * 这组方法都是 静态的同步方法,去加载具体的
     * <p>
     * 这个方法没有在静态代码块中
     *
     * @param clazz
     */
    public static synchronized void useCustomLogging(Class<? extends Log> clazz) {
        setImplementation(clazz);
    }

    public static synchronized void useSlf4jLogging() {
        //尝试设置实现
        setImplementation(com.ly.zmn48644.mybatis.logging.slf4j.Slf4jImpl.class);
    }

    public static synchronized void useCommonsLogging() {
        //尝试设置实现
        setImplementation(com.ly.zmn48644.mybatis.logging.commons.JakartaCommonsLoggingImpl.class);
    }

    public static synchronized void useLog4JLogging() {
        //尝试设置实现
        setImplementation(com.ly.zmn48644.mybatis.logging.log4j.Log4jImpl.class);
    }

    public static synchronized void useLog4J2Logging() {
        //尝试设置实现
        setImplementation(com.ly.zmn48644.mybatis.logging.log4j2.Log4j2Impl.class);
    }

    public static synchronized void useJdkLogging() {
        //尝试设置实现
        setImplementation(com.ly.zmn48644.mybatis.logging.jdk14.Jdk14LoggingImpl.class);
    }

    public static synchronized void useStdOutLogging() {
        //尝试设置实现
        setImplementation(com.ly.zmn48644.mybatis.logging.stdout.StdOutImpl.class);
    }

    public static synchronized void useNoLogging() {
        //尝试设置实现
        setImplementation(com.ly.zmn48644.mybatis.logging.nologging.NoLoggingImpl.class);
    }

    //尝试加载执行器
    private static void tryImplementation(Runnable runnable) {
        //如果没有找到合适的日志实现,才会执行
        if (logConstructor == null) {
            try {
                //注意这里竟然调用的是 run() 方法. 而不是start(),并没有开启新的线程
                runnable.run();
            } catch (Throwable t) {
                //这里忽略掉所有报错,setImplementation .
            }
        }
    }

    private static void setImplementation(Class<? extends Log> implClass) {
        try {

            //如果能够正常的获取到具体实现适配器的构造方法,说明可以使用.
            Constructor<? extends Log> candidate = implClass.getConstructor(String.class);
            Log log = candidate.newInstance(LogFactory.class.getName());
            if (log.isDebugEnabled()) {
                log.debug("Logging initialized using '" + implClass + "' adapter.");
            }
            //正常获取到指定实现适配器,logConstructor 就不在为空.
            logConstructor = candidate;
        } catch (Throwable t) {
            throw new LogException("Error setting Log implementation.  Cause: " + t, t);
        }
    }

}
