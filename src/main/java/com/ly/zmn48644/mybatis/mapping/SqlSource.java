
package com.ly.zmn48644.mybatis.mapping;

/**
 * 配置中的SQL语句将会被解析成 SqlSource 对象
 */
public interface SqlSource {

    BoundSql getBoundSql(Object parameterObject);

}
