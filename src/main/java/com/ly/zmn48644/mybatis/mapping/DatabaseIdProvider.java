
package com.ly.zmn48644.mybatis.mapping;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;


public interface DatabaseIdProvider {

  void setProperties(Properties p);

  String getDatabaseId(DataSource dataSource) throws SQLException;
}
