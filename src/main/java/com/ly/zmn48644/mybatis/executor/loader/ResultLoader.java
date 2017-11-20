
package com.ly.zmn48644.mybatis.executor.loader;

import com.ly.zmn48644.mybatis.cache.CacheKey;
import com.ly.zmn48644.mybatis.executor.Executor;
import com.ly.zmn48644.mybatis.executor.ExecutorException;
import com.ly.zmn48644.mybatis.executor.ResultExtractor;
import com.ly.zmn48644.mybatis.mapping.BoundSql;
import com.ly.zmn48644.mybatis.mapping.Environment;
import com.ly.zmn48644.mybatis.mapping.MappedStatement;
import com.ly.zmn48644.mybatis.reflection.factory.ObjectFactory;
import com.ly.zmn48644.mybatis.session.Configuration;
import com.ly.zmn48644.mybatis.session.ExecutorType;
import com.ly.zmn48644.mybatis.session.RowBounds;
import com.ly.zmn48644.mybatis.transaction.Transaction;
import com.ly.zmn48644.mybatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class ResultLoader {

  protected final Configuration configuration;
  protected final Executor executor;
  protected final MappedStatement mappedStatement;
  protected final Object parameterObject;
  protected final Class<?> targetType;
  protected final ObjectFactory objectFactory;
  protected final CacheKey cacheKey;
  protected final BoundSql boundSql;
  protected final ResultExtractor resultExtractor;
  protected final long creatorThreadId;
  
  protected boolean loaded;
  protected Object resultObject;
  
  public ResultLoader(Configuration config, Executor executor, MappedStatement mappedStatement, Object parameterObject, Class<?> targetType, CacheKey cacheKey, BoundSql boundSql) {
    this.configuration = config;
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.parameterObject = parameterObject;
    this.targetType = targetType;
    this.objectFactory = configuration.getObjectFactory();
    this.cacheKey = cacheKey;
    this.boundSql = boundSql;
    this.resultExtractor = new ResultExtractor(configuration, objectFactory);
    this.creatorThreadId = Thread.currentThread().getId();
  }

  public Object loadResult() throws SQLException {
    List<Object> list = selectList();
    resultObject = resultExtractor.extractObjectFromList(list, targetType);
    return resultObject;
  }

  private <E> List<E> selectList() throws SQLException {
    Executor localExecutor = executor;
    if (Thread.currentThread().getId() != this.creatorThreadId || localExecutor.isClosed()) {
      localExecutor = newExecutor();
    }
    try {
      return localExecutor.<E> query(mappedStatement, parameterObject, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, cacheKey, boundSql);
    } finally {
      if (localExecutor != executor) {
        localExecutor.close(false);
      }
    }
  }

  private Executor newExecutor() {
    final Environment environment = configuration.getEnvironment();
    if (environment == null) {
      throw new ExecutorException("ResultLoader could not load lazily.  Environment was not configured.");
    }
    final DataSource ds = environment.getDataSource();
    if (ds == null) {
      throw new ExecutorException("ResultLoader could not load lazily.  DataSource was not configured.");
    }
    final TransactionFactory transactionFactory = environment.getTransactionFactory();
    final Transaction tx = transactionFactory.newTransaction(ds, null, false);
    return configuration.newExecutor(tx, ExecutorType.SIMPLE);
  }

  public boolean wasNull() {
    return resultObject == null;
  }

}
