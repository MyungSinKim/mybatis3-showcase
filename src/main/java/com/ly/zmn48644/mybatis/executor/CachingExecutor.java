package com.ly.zmn48644.mybatis.executor;


import com.ly.zmn48644.mybatis.cache.Cache;
import com.ly.zmn48644.mybatis.cache.CacheKey;
import com.ly.zmn48644.mybatis.cache.TransactionalCacheManager;
import com.ly.zmn48644.mybatis.cursor.Cursor;
import com.ly.zmn48644.mybatis.mapping.*;
import com.ly.zmn48644.mybatis.reflection.MetaObject;
import com.ly.zmn48644.mybatis.session.ResultHandler;
import com.ly.zmn48644.mybatis.session.RowBounds;
import com.ly.zmn48644.mybatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * 其他类型执行器的缓存装饰器,用于二级(Mapper级别)缓存的实现.
 * 这里使用了 装饰模式
 * 缓存功能的执行器 会 装饰不同的 执行器实现
 * 比如: 缓存+批量 功能的执行器
 * 缓存+复用statement 功能的执行器
 * 缓存+基本 功能的执行器
 */
public class CachingExecutor implements Executor {

    //被装饰的底层执行器
    private final Executor delegate;
    //事务缓存管理器
    private final TransactionalCacheManager tcm = new TransactionalCacheManager();

    public CachingExecutor(Executor delegate) {
        //构造方法中设置被装饰的执行器
        this.delegate = delegate;
        delegate.setExecutorWrapper(this);
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    /**
     * 关闭事务
     *
     * @param forceRollback
     */
    @Override
    public void close(boolean forceRollback) {
        try {
            //issues #499, #524 and #573
            if (forceRollback) {
                tcm.rollback();
            } else {
                tcm.commit();
            }
        } finally {
            //关闭被装饰对象 数据库连接
            delegate.close(forceRollback);
        }
    }

    /**
     * 判断数据库连接是否关闭
     *
     * @return
     */
    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    /**
     * 执行更新操作
     *
     * @param ms
     * @param parameterObject
     * @return
     * @throws SQLException
     */
    @Override
    public int update(MappedStatement ms, Object parameterObject) throws SQLException {
        //刷新缓存
        flushCacheIfRequired(ms);
        //调用被装饰执行器执行更新
        return delegate.update(ms, parameterObject);
    }

    /**
     * 执行查询操作
     *
     * @param ms
     * @param parameterObject
     * @param rowBounds
     * @param resultHandler
     * @param <E>
     * @return
     * @throws SQLException
     */
    @Override
    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        //获取 封装具体执行 sql
        BoundSql boundSql = ms.getBoundSql(parameterObject);

        CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
        return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        flushCacheIfRequired(ms);
        return delegate.queryCursor(ms, parameter, rowBounds);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
            throws SQLException {
        Cache cache = ms.getCache();
        if (cache != null) {
            flushCacheIfRequired(ms);
            if (ms.isUseCache() && resultHandler == null) {
                ensureNoOutParams(ms, parameterObject, boundSql);
                //从缓存中查询
                List<E> list = (List<E>) tcm.getObject(cache, key);
                if (list == null) {
                    //如果缓存没有命中,调用被装饰执行器 , 从数据库中查询.
                    list = delegate.<E>query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
                    //将查询结果放入缓存
                    tcm.putObject(cache, key, list); // issue #578 and #116
                }
                //如果命中缓存直接返回缓存中数据.
                return list;
            }
        }
        //如果没有开启缓存,直接调用被装饰执行器查询数据库
        return delegate.<E>query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return delegate.flushStatements();
    }

    @Override
    public void commit(boolean required) throws SQLException {
        delegate.commit(required);
        tcm.commit();
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        try {
            delegate.rollback(required);
        } finally {
            if (required) {
                tcm.rollback();
            }
        }
    }

    private void ensureNoOutParams(MappedStatement ms, Object parameter, BoundSql boundSql) {
        if (ms.getStatementType() == StatementType.CALLABLE) {
            for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                if (parameterMapping.getMode() != ParameterMode.IN) {
                    throw new ExecutorException("Caching stored procedures with OUT params is not supported.  Please configure useCache=false in " + ms.getId() + " statement.");
                }
            }
        }
    }

    /**
     * 创建 CacheKey
     *
     * @param ms
     * @param parameterObject
     * @param rowBounds
     * @param boundSql
     * @return
     */
    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        //调用被装饰对象获取 CacheKey 对象
        return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    @Override
    public boolean isCached(MappedStatement ms, CacheKey key) {
        return delegate.isCached(ms, key);
    }

    @Override
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        delegate.deferLoad(ms, resultObject, property, key, targetType);
    }

    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    private void flushCacheIfRequired(MappedStatement ms) {
        Cache cache = ms.getCache();
        if (cache != null && ms.isFlushCacheRequired()) {
            tcm.clear(cache);
        }
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        throw new UnsupportedOperationException("This method should not be called");
    }

}
