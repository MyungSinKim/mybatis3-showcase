
package com.ly.zmn48644.mybatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 对数据库事务的封装接口
 *
 *
 */
public interface Transaction {

    /**
     * 获取数据库连接
     *
     * @return DataBase connection
     * @throws SQLException
     */
    Connection getConnection() throws SQLException;

    /**
     * 提交事务
     *
     * @throws SQLException
     */
    void commit() throws SQLException;

    /**
     * 回滚事务
     *
     * @throws SQLException
     */
    void rollback() throws SQLException;

    /**
     * 关闭数据库连接
     *
     * @throws SQLException
     */
    void close() throws SQLException;

    /**
     * 获取事务超时时间
     *
     * @throws SQLException
     */
    Integer getTimeout() throws SQLException;

}
