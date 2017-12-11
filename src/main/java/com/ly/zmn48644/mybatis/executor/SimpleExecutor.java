
package com.ly.zmn48644.mybatis.executor;


import com.ly.zmn48644.mybatis.cursor.Cursor;
import com.ly.zmn48644.mybatis.executor.statement.StatementHandler;
import com.ly.zmn48644.mybatis.logging.Log;
import com.ly.zmn48644.mybatis.mapping.BoundSql;
import com.ly.zmn48644.mybatis.mapping.MappedStatement;
import com.ly.zmn48644.mybatis.session.Configuration;
import com.ly.zmn48644.mybatis.session.ResultHandler;
import com.ly.zmn48644.mybatis.session.RowBounds;
import com.ly.zmn48644.mybatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * 默认的执行器
 * 使用 SimpleStatement 完成查询更新操作的执行器
 */
public class SimpleExecutor extends BaseExecutor {

    /**
     * 构造方法
     *
     * @param configuration 全局配置对象
     * @param transaction   事务管理器
     */
    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        //调用父类的构造
        super(configuration, transaction);
    }


    /**
     * 抽象父类 doUpdate 方法具体实现
     * @param ms
     * @param parameter
     * @return
     * @throws SQLException
     */
    @Override
    public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            //从 ms中获取 全局配置对象,但是 他的父类中已经有 全局配置对象属性了.
            Configuration configuration = ms.getConfiguration();
            //从全局配置对象中 获取  StatementHandler
            StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
            //创建 Statement
            stmt = prepareStatement(handler, ms.getStatementLog());
            //执行 Statement
            return handler.update(stmt);
        } finally {
            //执行完更新操作关闭 Statement
            closeStatement(stmt);
        }
    }


    /**
     * 抽象父类 doQuery方法的具体实现
     *
     * @param ms
     * @param parameter
     * @param rowBounds
     * @param resultHandler
     * @param boundSql
     * @param <E>
     * @return
     * @throws SQLException
     */
    @Override
    public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Statement stmt = null;
        try {
            //获取全局配置对象
            Configuration configuration = ms.getConfiguration();
            //从全局配置对象中获取 StatementHandler
            StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
            //预编译
            stmt = prepareStatement(handler, ms.getStatementLog());
            return handler.<E>query(stmt, resultHandler);
        } finally {
            closeStatement(stmt);
        }
    }

    @Override
    protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, null, boundSql);
        Statement stmt = prepareStatement(handler, ms.getStatementLog());
        return handler.<E>queryCursor(stmt);
    }

    /**
     * 当前类中doFlushStatements不做处理,直接返回空集合.
     * @param isRollback
     * @return
     * @throws SQLException
     */
    @Override
    public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
        return Collections.emptyList();
    }

    /**
     *
     * @param handler
     * @param statementLog
     * @return
     * @throws SQLException
     */
    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
        Statement stmt;
        Connection connection = getConnection(statementLog);
        stmt = handler.prepare(connection, transaction.getTimeout());
        handler.parameterize(stmt);
        return stmt;
    }

}
