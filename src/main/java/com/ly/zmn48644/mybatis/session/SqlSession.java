
package com.ly.zmn48644.mybatis.session;


import java.io.Closeable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * 此接口是MyBatis的业务逻辑核心接口
 * 此接口定义了MyBatis的核心功能
 * 此接口的实现类是 DefaultSqlSession
 * 此接口是从业务层面分析的入口
 */
public interface SqlSession extends Closeable {


    <T> T selectOne(String statement);

    <T> T selectOne(String statement, Object parameter);

    <E> List<E> selectList(String statement);

    <E> List<E> selectList(String statement, Object parameter);

    <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds);

    <K, V> Map<K, V> selectMap(String statement, String mapKey);

    <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey);

    <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds);
//TODO 临时注释 游标部分内容
//  /**
//   * A Cursor offers the same results as a List, except it fetches data lazily using an Iterator.
//   * @param <T> the returned cursor element type.
//   * @param statement Unique identifier matching the statement to use.
//   * @return Cursor of mapped objects
//   */
//  <T> Cursor<T> selectCursor(String statement);
//
//  /**
//   * A Cursor offers the same results as a List, except it fetches data lazily using an Iterator.
//   * @param <T> the returned cursor element type.
//   * @param statement Unique identifier matching the statement to use.
//   * @param parameter A parameter object to pass to the statement.
//   * @return Cursor of mapped objects
//   */
//  <T> Cursor<T> selectCursor(String statement, Object parameter);
//
//  /**
//   * A Cursor offers the same results as a List, except it fetches data lazily using an Iterator.
//   * @param <T> the returned cursor element type.
//   * @param statement Unique identifier matching the statement to use.
//   * @param parameter A parameter object to pass to the statement.
//   * @param rowBounds  Bounds to limit object retrieval
//   * @return Cursor of mapped objects
//   */
//  <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds);


    void select(String statement, Object parameter, ResultHandler handler);


    void select(String statement, ResultHandler handler);


    void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler);

    int insert(String statement);


    int insert(String statement, Object parameter);

    int update(String statement);

    int update(String statement, Object parameter);

    int delete(String statement);

    int delete(String statement, Object parameter);

    void commit();

    void commit(boolean force);

    void rollback();

    void rollback(boolean force);

    //TODO 临时注释 flushStatements
//  /**
//   * Flushes batch statements.
//   * @return BatchResult list of updated records
//   * @since 3.0.6
//   */
//  List<BatchResult> flushStatements();


    @Override
    void close();

    void clearCache();


    Configuration getConfiguration();


    <T> T getMapper(Class<T> type);

    /**
     * 获取内部的数据库连接对象
     * @return
     */
    Connection getConnection();
}
