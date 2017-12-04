
package com.ly.zmn48644.mybatis.executor.statement;


import com.ly.zmn48644.mybatis.cursor.Cursor;
import com.ly.zmn48644.mybatis.executor.Executor;
import com.ly.zmn48644.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.ly.zmn48644.mybatis.executor.keygen.KeyGenerator;
import com.ly.zmn48644.mybatis.executor.keygen.SelectKeyGenerator;
import com.ly.zmn48644.mybatis.mapping.BoundSql;
import com.ly.zmn48644.mybatis.mapping.MappedStatement;
import com.ly.zmn48644.mybatis.session.ResultHandler;
import com.ly.zmn48644.mybatis.session.RowBounds;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


/**
 * SimpleStatement,完成无参数
 */
public class SimpleStatementHandler extends BaseStatementHandler {

    /**
     * 创建 statementHandler
     * @param executor 执行器
     * @param mappedStatement 映射配置文件封装类
     * @param parameter
     * @param rowBounds
     * @param resultHandler
     * @param boundSql
     */
    public SimpleStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
    }

    @Override
    public int update(Statement statement) throws SQLException {
        String sql = boundSql.getSql();
        Object parameterObject = boundSql.getParameterObject();
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        int rows;
        //这里根据不同的主键生成器
        if (keyGenerator instanceof Jdbc3KeyGenerator) {
            statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
            rows = statement.getUpdateCount();
            keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
        } else if (keyGenerator instanceof SelectKeyGenerator) {
            //执行更新SQL
            statement.execute(sql);
            rows = statement.getUpdateCount();
            //插入数据后,执行获取主键SQL
            keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
        } else {
            //执行更新SQL
            statement.execute(sql);
            //获取更新影响行数
            rows = statement.getUpdateCount();
        }
        return rows;
    }

    @Override
    public void batch(Statement statement) throws SQLException {
        String sql = boundSql.getSql();
        //批量执行SQL
        statement.addBatch(sql);
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        //获取要执行的SQL语句
        String sql = boundSql.getSql();
        //执行SQL语句
        statement.execute(sql);
        //调用结果集处理器完成结果处理
        return resultSetHandler.<E>handleResultSets(statement);
    }

    @Override
    public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {
        String sql = boundSql.getSql();
        statement.execute(sql);
        return resultSetHandler.<E>handleCursorResultSets(statement);
    }

    /**
     * 此方法在 执行器中 执行 获取到 statemtn 对象后,在调用此类总的其他方法查询或者更新
     *
     * @param connection
     * @return
     * @throws SQLException
     */
    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        //如果设置了ResultSetType则 传入此参数.
        if (mappedStatement.getResultSetType() != null) {
            return connection.createStatement(mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
        } else {
            //创建普通的不支持参数的 statement 对象
            return connection.createStatement();
        }
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        // 由于 instantiateStatement  方法中创建的是 SimpleStatement 是不支持参数的因此
        // 此方法为空.
    }

}
