
package com.ly.zmn48644.mybatis.session.defaults;


import com.ly.zmn48644.mybatis.binding.BindingException;
import com.ly.zmn48644.mybatis.cursor.Cursor;
import com.ly.zmn48644.mybatis.exceptions.ExceptionFactory;
import com.ly.zmn48644.mybatis.exceptions.TooManyResultsException;
import com.ly.zmn48644.mybatis.executor.BatchResult;
import com.ly.zmn48644.mybatis.executor.ErrorContext;
import com.ly.zmn48644.mybatis.executor.Executor;
import com.ly.zmn48644.mybatis.executor.result.DefaultMapResultHandler;
import com.ly.zmn48644.mybatis.executor.result.DefaultResultContext;
import com.ly.zmn48644.mybatis.mapping.MappedStatement;
import com.ly.zmn48644.mybatis.session.Configuration;
import com.ly.zmn48644.mybatis.session.ResultHandler;
import com.ly.zmn48644.mybatis.session.RowBounds;
import com.ly.zmn48644.mybatis.session.SqlSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;


/**
 * 实现SqlSession接口,MyBatis核心业务逻辑的具体实现
 */
public class DefaultSqlSession implements SqlSession {

    //全局配置对象
    private final Configuration configuration;

    //Statement 执行器
    private final Executor executor;
    //是否自动提价事务
    private final boolean autoCommit;

    private boolean dirty;

    private List<Cursor<?>> cursorList;

    public DefaultSqlSession(Configuration configuration, Executor executor, boolean autoCommit) {
        this.configuration = configuration;
        this.executor = executor;
        this.dirty = false;
        this.autoCommit = autoCommit;
    }

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this(configuration, executor, false);
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.<T>selectOne(statement, null);
    }

    /**
     * 执行查询只有一个返回结果,底层调用的是 selectList 方法
     *
     * @param statement
     * @param parameter
     * @param <T>
     * @return
     */
    @Override
    public <T> T selectOne(String statement, Object parameter) {
        // Popular vote was to return null on 0 results and throw exception on too many.
        //这里调用 selectList 方法
        List<T> list = this.<T>selectList(statement, parameter);

        if (list.size() == 1) {
            //如果 查询结果长度为1,返回第一个.
            return list.get(0);
        } else if (list.size() > 1) {
            //如果返回多个 抛出异常.
            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        } else {
            //返回其他长度值,返回null
            return null;
        }
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        return this.selectMap(statement, null, mapKey, RowBounds.DEFAULT);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return this.selectMap(statement, parameter, mapKey, RowBounds.DEFAULT);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
        final List<? extends V> list = selectList(statement, parameter, rowBounds);
        final DefaultMapResultHandler<K, V> mapResultHandler = new DefaultMapResultHandler<K, V>(mapKey,
                configuration.getObjectFactory(), configuration.getObjectWrapperFactory(), configuration.getReflectorFactory());
        final DefaultResultContext<V> context = new DefaultResultContext<V>();
        for (V o : list) {
            context.nextResultObject(o);
            mapResultHandler.handleResult(context);
        }
        return mapResultHandler.getMappedResults();
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement) {
        return null;
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter) {
        return null;
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
        return null;
    }


    @Override
    public <E> List<E> selectList(String statement) {
        return this.selectList(statement, null);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.selectList(statement, parameter, RowBounds.DEFAULT);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        try {
            //从全局配置中心获取 MappedStatement 根据 statement .
            MappedStatement ms = configuration.getMappedStatement(statement);
            //调用底层执行器方法执行 statement .
            return executor.query(ms, wrapCollection(parameter), rowBounds, Executor.NO_RESULT_HANDLER);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public void select(String statement, Object parameter, ResultHandler handler) {
        select(statement, parameter, RowBounds.DEFAULT, handler);
    }

    @Override
    public void select(String statement, ResultHandler handler) {
        select(statement, null, RowBounds.DEFAULT, handler);
    }

    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        try {
            MappedStatement ms = configuration.getMappedStatement(statement);
            executor.query(ms, wrapCollection(parameter), rowBounds, handler);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public int insert(String statement) {
        return insert(statement, null);
    }

    @Override
    public int insert(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public int update(String statement) {
        return update(statement, null);
    }

    @Override
    public int update(String statement, Object parameter) {
        try {
            dirty = true;
            MappedStatement ms = configuration.getMappedStatement(statement);
            return executor.update(ms, wrapCollection(parameter));
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error updating database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public int delete(String statement) {
        return update(statement, null);
    }

    @Override
    public int delete(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public void commit() {
        commit(false);
    }

    @Override
    public void commit(boolean force) {
        try {
            executor.commit(isCommitOrRollbackRequired(force));
            dirty = false;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error committing transaction.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public void rollback() {
        rollback(false);
    }

    @Override
    public void rollback(boolean force) {
        try {
            executor.rollback(isCommitOrRollbackRequired(force));
            dirty = false;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error rolling back transaction.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public List<BatchResult> flushStatements() {
        try {
            return executor.flushStatements();
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error flushing statements.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public void close() {
        try {
            executor.close(isCommitOrRollbackRequired(false));
            closeCursors();
            dirty = false;
        } finally {
            ErrorContext.instance().reset();
        }
    }

    private void closeCursors() {
        if (cursorList != null && cursorList.size() != 0) {
            for (Cursor<?> cursor : cursorList) {
                try {
                    cursor.close();
                } catch (IOException e) {
                    throw ExceptionFactory.wrapException("Error closing cursor.  Cause: " + e, e);
                }
            }
            cursorList.clear();
        }
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.<T>getMapper(type, this);
    }

    @Override
    public Connection getConnection() {
        try {
            return executor.getTransaction().getConnection();
        } catch (SQLException e) {
            throw ExceptionFactory.wrapException("Error getting a new connection.  Cause: " + e, e);
        }
    }

    @Override
    public void clearCache() {
        executor.clearLocalCache();
    }

    private <T> void registerCursor(Cursor<T> cursor) {
        if (cursorList == null) {
            cursorList = new ArrayList<Cursor<?>>();
        }
        cursorList.add(cursor);
    }

    private boolean isCommitOrRollbackRequired(boolean force) {
        return (!autoCommit && dirty) || force;
    }

    /***
     *
     * @param object
     * @return
     */
    private Object wrapCollection(final Object object) {
        /**
         * 传入的object有可能是下面几种类型.
         * 第一种: 仅仅是一个参数值, 比如 接口方法是  xxx(String name)
         * 第二种: object是一个Map, 比如 接口方法是 xxx(String id,String name)
         * 第三种: object是一个 list , 比如 接口方法是 xxx(List<String> nameList)
         * 第四种: object是一个 Array, 比如 接口方法是 xxx(String[] nameArray)
         */

        if (object instanceof Collection) {
            //如果是 Collecion 类型
            StrictMap<Object> map = new StrictMap<Object>();
            map.put("collection", object);
            if (object instanceof List) {
                //如果是 list 类型
                map.put("list", object);
            }
            return map;
            //判断 object 是否是数组类型
        } else if (object != null && object.getClass().isArray()) {
            StrictMap<Object> map = new StrictMap<Object>();
            //是 数组类型
            map.put("array", object);
            return map;
        }
        return object;
    }

    public static class StrictMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -5741767162221585340L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + this.keySet());
            }
            return super.get(key);
        }

    }

}
