
package com.ly.zmn48644.mybatis.executor;


import com.ly.zmn48644.mybatis.cache.CacheKey;
import com.ly.zmn48644.mybatis.cursor.Cursor;
import com.ly.zmn48644.mybatis.mapping.BoundSql;
import com.ly.zmn48644.mybatis.mapping.MappedStatement;
import com.ly.zmn48644.mybatis.reflection.MetaObject;
import com.ly.zmn48644.mybatis.session.ResultHandler;
import com.ly.zmn48644.mybatis.session.RowBounds;
import com.ly.zmn48644.mybatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * 执行器接口
 * <p>
 * 更新操作,上层SqlSession中的 update delete insert 底层都是调用此方法.
 * 查询操作,上层SqlSession中的 selectOne,selectList,selectOne 等 都是调用此方法
 */
public interface Executor {

    ResultHandler NO_RESULT_HANDLER = null;

    /**
     * 执行更新操作
     *
     * @param ms        全局配置对象中维护的 MappedStatement 对象
     * @param parameter 在 SqlSession 中被包装过得 参数对象
     * @return 返回影响行数
     * @throws SQLException
     */
    int update(MappedStatement ms, Object parameter) throws SQLException;

    /**
     * 执行查询操作
     *
     * @param ms
     * @param parameter
     * @param rowBounds
     * @param resultHandler
     * @param cacheKey
     * @param boundSql
     * @param <E>
     * @return
     * @throws SQLException
     */
    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;

    <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException;


    /**
     * 在 ReuseExecutor 中有具体的有意义的实现,就是关闭所有缓存的 Statement 对象.
     *
     * @return
     * @throws SQLException
     */
    List<BatchResult> flushStatements() throws SQLException;

    /**
     * 提交事务
     *
     * @param required
     * @throws SQLException
     */
    void commit(boolean required) throws SQLException;

    /**
     * 回滚事务
     *
     * @param required
     * @throws SQLException
     */
    void rollback(boolean required) throws SQLException;

    CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

    boolean isCached(MappedStatement ms, CacheKey key);

    /**
     * 清除一级缓存
     */
    void clearLocalCache();

    void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

    Transaction getTransaction();

    void close(boolean forceRollback);

    boolean isClosed();

    /**
     * 当 次执行器被包装时,设定外层的包装执行器.
     *
     * @param executor
     */
    void setExecutorWrapper(Executor executor);

}
