
package com.ly.zmn48644.mybatis.scripting.xmltags;


import com.ly.zmn48644.mybatis.builder.SqlSourceBuilder;
import com.ly.zmn48644.mybatis.mapping.BoundSql;
import com.ly.zmn48644.mybatis.mapping.SqlSource;
import com.ly.zmn48644.mybatis.session.Configuration;

import java.util.Map;

public class DynamicSqlSource implements SqlSource {

    private Configuration configuration;
    private SqlNode rootSqlNode;

    public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
        this.configuration = configuration;
        this.rootSqlNode = rootSqlNode;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        DynamicContext context = new DynamicContext(configuration, parameterObject);
        rootSqlNode.apply(context);
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();


        SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());

        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
        return boundSql;
    }

}
