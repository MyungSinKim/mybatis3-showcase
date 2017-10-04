
package com.ly.zmn48644.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 所有的类型转换器都会实现此接口,用于定义类型转换器的行为.
 *
 * @param <T>
 */
public interface TypeHandler<T> {

    /**
     * 通过 PreparedStatement 为SQL语句绑定参数时,会将数据由 JdbcType 类型转换为 java 类型
     *
     * @param ps
     * @param i
     * @param parameter
     * @param jdbcType
     * @throws SQLException
     */
    void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

    /**
     * 从 ResultSet 中获取数据时调用此方法,会将数据从 JdbcType 类型转换为 java 类型
     * 这个方法是根据 列名获取.
     *
     * @param rs
     * @param columnName
     * @return
     * @throws SQLException
     */
    T getResult(ResultSet rs, String columnName) throws SQLException;

    /**
     * 从 ResultSet 中获取数据时调用此方法,会将数据从 JdbcType 类型转换为 java 类型
     * 这个方法是根据 列索引获取.
     *
     * @param rs
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    T getResult(ResultSet rs, int columnIndex) throws SQLException;

    /**
     * 从存储过程中获取数据调用此方法,会将数据从 JdbcType 类型转换为 java 类型
     *
     * @param cs
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    T getResult(CallableStatement cs, int columnIndex) throws SQLException;

}
