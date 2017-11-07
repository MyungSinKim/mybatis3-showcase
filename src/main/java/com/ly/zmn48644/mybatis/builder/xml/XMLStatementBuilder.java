
package com.ly.zmn48644.mybatis.builder.xml;


import com.ly.zmn48644.mybatis.builder.BaseBuilder;
import com.ly.zmn48644.mybatis.builder.MapperBuilderAssistant;
import com.ly.zmn48644.mybatis.parsing.XNode;
import com.ly.zmn48644.mybatis.session.Configuration;

public class XMLStatementBuilder extends BaseBuilder {

    private final MapperBuilderAssistant builderAssistant;
    private final XNode context;
    private final String requiredDatabaseId;

    public XMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, XNode context) {
        this(configuration, builderAssistant, context, null);
    }

    public XMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, XNode context, String databaseId) {
        super(configuration);
        this.builderAssistant = builderAssistant;
        this.context = context;
        this.requiredDatabaseId = databaseId;
    }

    public void parseStatementNode() {

    }


}
