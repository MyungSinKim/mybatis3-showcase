
package com.ly.zmn48644.mybatis.logging;


import com.ly.zmn48644.mybatis.logging.commons.JakartaCommonsLoggingImpl;
import com.ly.zmn48644.mybatis.logging.jdk14.Jdk14LoggingImpl;
import com.ly.zmn48644.mybatis.logging.log4j.Log4jImpl;
import com.ly.zmn48644.mybatis.logging.log4j2.Log4j2Impl;
import com.ly.zmn48644.mybatis.logging.nologging.NoLoggingImpl;
import com.ly.zmn48644.mybatis.logging.slf4j.Slf4jImpl;
import com.ly.zmn48644.mybatis.logging.stdout.StdOutImpl;
import org.junit.Test;



import static org.junit.Assert.assertEquals;

public class LogFactoryTest {

  @Test
  public void shouldUseCommonsLogging() {
    LogFactory.useCommonsLogging();
    Log log = LogFactory.getLog(Object.class);
    logSomething(log);
    assertEquals(log.getClass().getName(), JakartaCommonsLoggingImpl.class.getName());
  }

  @Test
  public void shouldUseLog4J() {
    LogFactory.useLog4JLogging();
    Log log = LogFactory.getLog(Object.class);
    logSomething(log);
    assertEquals(log.getClass().getName(), Log4jImpl.class.getName());
  }

  @Test
  public void shouldUseLog4J2() {
    LogFactory.useLog4J2Logging();
    Log log = LogFactory.getLog(Object.class);
    logSomething(log);
    assertEquals(log.getClass().getName(), Log4j2Impl.class.getName());
  }
  
  @Test
  public void shouldUseJdKLogging() {
    LogFactory.useJdkLogging();
    Log log = LogFactory.getLog(Object.class);
    logSomething(log);
    assertEquals(log.getClass().getName(), Jdk14LoggingImpl.class.getName());
  }

  @Test
  public void shouldUseSlf4j() {
    LogFactory.useSlf4jLogging();
    Log log = LogFactory.getLog(Object.class);
    logSomething(log);
    assertEquals(log.getClass().getName(), Slf4jImpl.class.getName());
  }

  @Test
  public void shouldUseStdOut() {
    LogFactory.useStdOutLogging();
    Log log = LogFactory.getLog(Object.class);
    logSomething(log);
    assertEquals(log.getClass().getName(), StdOutImpl.class.getName());
  }

  @Test
  public void shouldUseNoLogging() {
    LogFactory.useNoLogging();
    Log log = LogFactory.getLog(Object.class);
    logSomething(log);
    assertEquals(log.getClass().getName(), NoLoggingImpl.class.getName());
  }

//  @Test
//  public void shouldReadLogImplFromSettings() throws Exception {
//    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/logging/mybatis-config.xml");
//    new SqlSessionFactoryBuilder().build(reader);
//    reader.close();
//
//    Log log = LogFactory.getLog(Object.class);
//    log.debug("Debug message.");
//    assertEquals(log.getClass().getName(), NoLoggingImpl.class.getName());
//  }

  private void logSomething(Log log) {
    log.warn("Warning message.");
    log.debug("Debug message.");
    log.error("Error message.");
    log.error("Error with Exception.", new Exception("Test exception."));
  }

}
