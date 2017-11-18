
package com.ly.zmn48644.mybatis.scripting.xmltags;

import java.util.List;


public class MixedSqlNode implements SqlNode {
    private List<SqlNode> contents;

    public MixedSqlNode(List<SqlNode> contents) {
        this.contents = contents;
    }

    @Override
    public boolean apply(DynamicContext context) {
        for (SqlNode sqlNode : contents) {
            sqlNode.apply(context);
        }
        return true;
    }
}
