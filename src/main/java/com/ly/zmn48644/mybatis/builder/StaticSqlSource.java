
package com.ly.zmn48644.mybatis.builder;


import com.ly.zmn48644.mybatis.mapping.BoundSql;
import com.ly.zmn48644.mybatis.mapping.ParameterMapping;
import com.ly.zmn48644.mybatis.mapping.SqlSource;
import com.ly.zmn48644.mybatis.session.Configuration;

import java.util.List;

/**
 * 封装静态SQL语句
 *
 */
public class StaticSqlSource implements SqlSource {

    //可能包含? 的sql语句,可以直接发送给
    //数据库进行执行
    private String sql;
    //参数映射
    private List<ParameterMapping> parameterMappings;
    //全局配置对象
    private Configuration configuration;

    public StaticSqlSource(Configuration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }

}
