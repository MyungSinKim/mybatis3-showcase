
package com.ly.zmn48644.mybatis.type;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * JdbcType 枚举类
 */
public enum JdbcType {

    ARRAY(Types.ARRAY),
    BIT(Types.BIT),
    TINYINT(Types.TINYINT),
    SMALLINT(Types.SMALLINT),
    INTEGER(Types.INTEGER),
    BIGINT(Types.BIGINT),
    FLOAT(Types.FLOAT),
    REAL(Types.REAL),
    DOUBLE(Types.DOUBLE),
    NUMERIC(Types.NUMERIC),
    DECIMAL(Types.DECIMAL),
    CHAR(Types.CHAR),
    VARCHAR(Types.VARCHAR),
    LONGVARCHAR(Types.LONGVARCHAR),
    DATE(Types.DATE),
    TIME(Types.TIME),
    TIMESTAMP(Types.TIMESTAMP),
    BINARY(Types.BINARY),
    VARBINARY(Types.VARBINARY),
    LONGVARBINARY(Types.LONGVARBINARY),
    NULL(Types.NULL),
    OTHER(Types.OTHER),
    BLOB(Types.BLOB),
    CLOB(Types.CLOB),
    BOOLEAN(Types.BOOLEAN),
    CURSOR(-10), // Oracle
    UNDEFINED(Integer.MIN_VALUE + 1000),
    NVARCHAR(Types.NVARCHAR), // JDK6
    NCHAR(Types.NCHAR), // JDK6
    NCLOB(Types.NCLOB), // JDK6
    STRUCT(Types.STRUCT),
    JAVA_OBJECT(Types.JAVA_OBJECT),
    DISTINCT(Types.DISTINCT),
    REF(Types.REF),
    DATALINK(Types.DATALINK),
    ROWID(Types.ROWID), // JDK6
    LONGNVARCHAR(Types.LONGNVARCHAR), // JDK6
    SQLXML(Types.SQLXML), // JDK6
    DATETIMEOFFSET(-155); // SQL Server 2008

    public final int TYPE_CODE;

    //维护 常量编码(Types)与JdbcType中个中类型的映射关系
    private static Map<Integer, JdbcType> codeLookup = new HashMap<Integer, JdbcType>();


    /**
     * 类加载时 执行 向codeLookup中添加常量编码和JdbcType枚举的映射
     */
    static {
        for (JdbcType type : JdbcType.values()) {
            codeLookup.put(type.TYPE_CODE, type);
        }
    }

    /**
     * 根据常量编码创建 JdbcType 枚举
     *
     * @param code
     */
    JdbcType(int code) {
        this.TYPE_CODE = code;
    }

    /**
     * 根据常量编码获取,JdbcType枚举
     *
     * @param code
     * @return
     */
    public static JdbcType forCode(int code) {
        return codeLookup.get(code);
    }

}
