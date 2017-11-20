
package com.ly.zmn48644.mybatis.executor.resultset;


import com.ly.zmn48644.mybatis.cursor.Cursor;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


public interface ResultSetHandler {

    <E> List<E> handleResultSets(Statement stmt) throws SQLException;

    <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException;

    void handleOutputParameters(CallableStatement cs) throws SQLException;

}
