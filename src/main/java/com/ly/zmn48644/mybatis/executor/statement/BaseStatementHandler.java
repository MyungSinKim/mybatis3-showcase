
package com.ly.zmn48644.mybatis.executor.statement;


import com.ly.zmn48644.mybatis.executor.ErrorContext;
import com.ly.zmn48644.mybatis.executor.Executor;
import com.ly.zmn48644.mybatis.executor.ExecutorException;
import com.ly.zmn48644.mybatis.executor.keygen.KeyGenerator;
import com.ly.zmn48644.mybatis.executor.parameter.ParameterHandler;
import com.ly.zmn48644.mybatis.executor.resultset.ResultSetHandler;
import com.ly.zmn48644.mybatis.mapping.BoundSql;
import com.ly.zmn48644.mybatis.mapping.MappedStatement;
import com.ly.zmn48644.mybatis.reflection.factory.ObjectFactory;
import com.ly.zmn48644.mybatis.session.Configuration;
import com.ly.zmn48644.mybatis.session.ResultHandler;
import com.ly.zmn48644.mybatis.session.RowBounds;
import com.ly.zmn48644.mybatis.type.TypeHandlerRegistry;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 这里此类中 prepare 方法的地方使用了模板方法模式
 */
public abstract class BaseStatementHandler implements StatementHandler {
    //全局配置对象
    protected final Configuration configuration;
    //对象工厂
    protected final ObjectFactory objectFactory;
    //类型转换处理器注册中心
    protected final TypeHandlerRegistry typeHandlerRegistry;
    //结果集处理器
    protected final ResultSetHandler resultSetHandler;
    //参数处理器
    protected final ParameterHandler parameterHandler;

    //执行器
    protected final Executor executor;

    protected final MappedStatement mappedStatement;

    //结果行数限定对象
    protected final RowBounds rowBounds;

    //封装具体执行的SQL语句
    protected BoundSql boundSql;

    protected BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;

        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.objectFactory = configuration.getObjectFactory();

        if (boundSql == null) { // issue #435, get the key before calculating the statement
            generateKeys(parameterObject);
            boundSql = mappedStatement.getBoundSql(parameterObject);
        }

        this.boundSql = boundSql;

        this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
        this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds, parameterHandler, resultHandler, boundSql);
    }

    @Override
    public BoundSql getBoundSql() {
        return boundSql;
    }

    @Override
    public ParameterHandler getParameterHandler() {
        return parameterHandler;
    }


    /**
     * 下面是一个模板方法,在创建statement的过程中
     * instantiateStatement 方法的部分委托给了其实现类
     *
     * @param connection
     * @param transactionTimeout
     * @return
     * @throws SQLException
     */
    @Override
    public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
        ErrorContext.instance().sql(boundSql.getSql());
        Statement statement = null;
        try {
            //调用子类方法,完成具体Statement的创建.
            statement = instantiateStatement(connection);
            //设置事务超时时间
            setStatementTimeout(statement, transactionTimeout);
            //设置FetchSize
            setFetchSize(statement);
            return statement;
        } catch (SQLException e) {
            //抛出异常关闭statement
            closeStatement(statement);
            throw e;
        } catch (Exception e) {
            //抛出异常关闭statement
            closeStatement(statement);
            throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
        }
    }

    /**
     * 抽象方法委托给具体实现类完成创建操作
     *
     * @param connection
     * @return
     * @throws SQLException
     */
    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

    protected void setStatementTimeout(Statement stmt, Integer transactionTimeout) throws SQLException {
        Integer queryTimeout = null;
        if (mappedStatement.getTimeout() != null) {
            queryTimeout = mappedStatement.getTimeout();
        } else if (configuration.getDefaultStatementTimeout() != null) {
            queryTimeout = configuration.getDefaultStatementTimeout();
        }
        if (queryTimeout != null) {
            stmt.setQueryTimeout(queryTimeout);
        }
        StatementUtil.applyTransactionTimeout(stmt, queryTimeout, transactionTimeout);
    }

    protected void setFetchSize(Statement stmt) throws SQLException {
        Integer fetchSize = mappedStatement.getFetchSize();
        if (fetchSize != null) {
            stmt.setFetchSize(fetchSize);
            return;
        }
        Integer defaultFetchSize = configuration.getDefaultFetchSize();
        if (defaultFetchSize != null) {
            stmt.setFetchSize(defaultFetchSize);
        }
    }

    protected void closeStatement(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            //ignore
        }
    }

    protected void generateKeys(Object parameter) {
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        ErrorContext.instance().store();
        keyGenerator.processBefore(executor, mappedStatement, null, parameter);
        ErrorContext.instance().recall();
    }

}
