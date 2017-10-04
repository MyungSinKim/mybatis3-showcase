
package com.ly.zmn48644.mybatis.type;

import java.sql.*;
import java.time.LocalTime;


 //@UsesJava8
public class LocalTimeTypeHandler extends BaseTypeHandler<LocalTime> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, LocalTime parameter, JdbcType jdbcType)
          throws SQLException {
    ps.setTime(i, Time.valueOf(parameter));
  }

  @Override
  public LocalTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
    Time time = rs.getTime(columnName);
    return getLocalTime(time);
  }

  @Override
  public LocalTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    Time time = rs.getTime(columnIndex);
    return getLocalTime(time);
  }

  @Override
  public LocalTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    Time time = cs.getTime(columnIndex);
    return getLocalTime(time);
  }

  private static LocalTime getLocalTime(Time time) {
    if (time != null) {
      return time.toLocalTime();
    }
    return null;
  }
}
