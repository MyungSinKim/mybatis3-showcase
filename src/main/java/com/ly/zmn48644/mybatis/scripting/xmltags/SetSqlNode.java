
package com.ly.zmn48644.mybatis.scripting.xmltags;



import com.ly.zmn48644.mybatis.session.Configuration;

import java.util.Arrays;
import java.util.List;


public class SetSqlNode extends TrimSqlNode {

  private static List<String> suffixList = Arrays.asList(",");

  public SetSqlNode(Configuration configuration, SqlNode contents) {
    super(configuration, contents, "SET", null, null, suffixList);
  }

}
