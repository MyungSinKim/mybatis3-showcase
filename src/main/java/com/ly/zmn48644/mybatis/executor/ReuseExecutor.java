
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 重用statement的执行器,在 SqlSession 级别进行重用
 *
 * 如何验证在同一个SqlSession中执行相同的SQL语句??
 * 查看 两次使用的statement是否是同一个.
 *
 */
public class ReuseExecutor extends BaseExecutor {

    /**
     * 用于缓存 statement , key 为执行的sql语句
     */
    private final Map<String, Statement> statementMap = new HashMap<String, Statement>();

    public ReuseExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
        Statement stmt = prepareStatement(handler, ms.getStatementLog());
        return handler.update(stmt);
    }

    @Override
    public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
        Statement stmt = prepareStatement(handler, ms.getStatementLog());
        return handler.<E>query(stmt, resultHandler);
    }

    @Override
    protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, null, boundSql);
        Statement stmt = prepareStatement(handler, ms.getStatementLog());
        return handler.<E>queryCursor(stmt);
    }

    @Override
    public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
        for (Statement stmt : statementMap.values()) {
            closeStatement(stmt);
        }
        statementMap.clear();
        return Collections.emptyList();
    }

    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
        Statement stmt;
        BoundSql boundSql = handler.getBoundSql();
        String sql = boundSql.getSql();
        if (hasStatementFor(sql)) {
            stmt = getStatement(sql);
            applyTransactionTimeout(stmt);
        } else {
            Connection connection = getConnection(statementLog);
            stmt = handler.prepare(connection, transaction.getTimeout());
            /**
             * 向 statementMap 中添加以后可以重用的 stmt
             * 最开始几次查询 statementMap 为空 因此都会执行到此
             * 当多次执行后,开始重复执行之前执行过的sql时,会走另外一个重用statement的分支.
             */
            putStatement(sql, stmt);
        }
        handler.parameterize(stmt);
        return stmt;
    }

    /**
     * 判断当前执行的Sql能否重用 statement
     *
     * @param sql
     * @return
     */
    private boolean hasStatementFor(String sql) {
        try {
            return statementMap.keySet().contains(sql) && !statementMap.get(sql).getConnection().isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 从 statementMap 获取 statement
     *
     * @param s
     * @return
     */
    private Statement getStatement(String s) {
        return statementMap.get(s);
    }

    /**
     * 向 statementMap 中添加 statement
     *
     * @param sql
     * @param stmt
     */
    private void putStatement(String sql, Statement stmt) {
        statementMap.put(sql, stmt);
    }

}
