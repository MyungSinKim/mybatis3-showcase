
package com.ly.zmn48644.mybatis.executor.resultset;


import com.ly.zmn48644.mybatis.cursor.Cursor;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 在StatementHandler执行完select语句之后,会将查询结果交给
 * ResultSetHandler进行结果处理,包含下面三种.
 * 普通结果
 * 存储过程结果
 * 游标结果
 *
 * DefaultResultSetHandler是MyBatis提供的结果集处理实现
 *
 */
public interface ResultSetHandler {
    /**
     * 处理结果集,生成相应的结果对象集合.
     *
     * @param stmt
     * @param <E>
     * @return
     * @throws SQLException
     */
    <E> List<E> handleResultSets(Statement stmt) throws SQLException;

    /**
     * 处理结果集,生成相应的游标对象
     *
     * @param stmt
     * @param <E>
     * @return
     * @throws SQLException
     */
    <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException;

    /**
     * 处理存储过程的输出参数
     *
     * @param cs
     * @throws SQLException
     */
    void handleOutputParameters(CallableStatement cs) throws SQLException;

}
