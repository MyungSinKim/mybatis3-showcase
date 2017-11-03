
package com.ly.zmn48644.mybatis.scripting;


import com.ly.zmn48644.mybatis.executor.parameter.ParameterHandler;
import com.ly.zmn48644.mybatis.mapping.BoundSql;
import com.ly.zmn48644.mybatis.mapping.MappedStatement;
import com.ly.zmn48644.mybatis.mapping.SqlSource;
import com.ly.zmn48644.mybatis.parsing.XNode;
import com.ly.zmn48644.mybatis.session.Configuration;

public interface LanguageDriver {


    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);


    SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType);


    SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

}
