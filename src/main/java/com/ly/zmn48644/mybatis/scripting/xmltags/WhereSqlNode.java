
package com.ly.zmn48644.mybatis.scripting.xmltags;

import com.ly.zmn48644.mybatis.session.Configuration;

import java.util.Arrays;
import java.util.List;


public class WhereSqlNode extends TrimSqlNode {

    private static List<String> prefixList = Arrays.asList("AND ", "OR ", "AND\n", "OR\n", "AND\r", "OR\r", "AND\t", "OR\t");

    public WhereSqlNode(Configuration configuration, SqlNode contents) {
        super(configuration, contents, "WHERE", prefixList, null, null);
    }

}
