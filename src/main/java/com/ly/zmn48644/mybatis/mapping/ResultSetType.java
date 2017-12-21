
package com.ly.zmn48644.mybatis.mapping;

import java.sql.ResultSet;

/***
 * Connection 提供了三个重载的方法来创建 Statement
 */
public enum ResultSetType {
    FORWARD_ONLY(ResultSet.TYPE_FORWARD_ONLY),

    SCROLL_INSENSITIVE(ResultSet.TYPE_SCROLL_INSENSITIVE),
    SCROLL_SENSITIVE(ResultSet.TYPE_SCROLL_SENSITIVE);
    private final int value;

    ResultSetType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
