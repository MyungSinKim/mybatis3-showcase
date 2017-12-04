
package com.ly.zmn48644.mybatis.executor.statement;

import com.ly.zmn48644.mybatis.cursor.Cursor;
import com.ly.zmn48644.mybatis.executor.parameter.ParameterHandler;
import com.ly.zmn48644.mybatis.mapping.BoundSql;
import com.ly.zmn48644.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 在此接口中的实现类中 实现  Statement 的创建 以及 执行具体的 SQL
 */
public interface StatementHandler {
    /**
     * 创建  statemnt 对象
     * 不同的 StatementHandler 具体实现,创建的Statement对象也不同
     *
     * @param connection
     * @param transactionTimeout
     * @return
     * @throws SQLException
     */
    Statement prepare(Connection connection, Integer transactionTimeout)
            throws SQLException;

    /**
     * 在此方法完成对 statement 对象进行执行参数设置
     *
     * @param statement
     * @throws SQLException
     */
    void parameterize(Statement statement)
            throws SQLException;

    /**
     * 调用 statement 完成批量执行SQL语句
     *
     * @param statement
     * @throws SQLException
     */
    void batch(Statement statement)
            throws SQLException;

    /**
     * 调用 statement 完成更新操作
     *
     * @param statement
     * @return
     * @throws SQLException
     */
    int update(Statement statement)
            throws SQLException;

    /**
     * 调用 statement 查询方法完成普通查询,通过resultHandler处理结果集
     *
     * @param statement
     * @param resultHandler
     * @param <E>
     * @return
     * @throws SQLException
     */
    <E> List<E> query(Statement statement, ResultHandler resultHandler)
            throws SQLException;

    /**
     * 调用 statement 完成游标查询,返回的是游标对象
     *
     * @param statement
     * @param <E>
     * @return
     * @throws SQLException
     */
    <E> Cursor<E> queryCursor(Statement statement)
            throws SQLException;

    /**
     * 获取 BoundSql 对象
     *
     * @return
     */
    BoundSql getBoundSql();

    /**
     * 获取参数处理器
     *
     * @return
     */
    ParameterHandler getParameterHandler();

}
