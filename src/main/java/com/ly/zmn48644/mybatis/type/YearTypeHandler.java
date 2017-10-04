
package com.ly.zmn48644.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;


// //@UsesJava8
public class YearTypeHandler extends BaseTypeHandler<Year> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Year year, JdbcType type) throws SQLException {
        ps.setInt(i, year.getValue());
    }

    @Override
    public Year getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int year = rs.getInt(columnName);
        return year == 0 ? null : Year.of(year);
    }

    @Override
    public Year getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int year = rs.getInt(columnIndex);
        return year == 0 ? null : Year.of(year);
    }

    @Override
    public Year getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int year = cs.getInt(columnIndex);
        return year == 0 ? null : Year.of(year);
    }

}
