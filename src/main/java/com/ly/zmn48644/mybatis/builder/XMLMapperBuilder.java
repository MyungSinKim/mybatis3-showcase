package com.ly.zmn48644.mybatis.builder;

import com.ly.zmn48644.mybatis.builder.xml.XMLMapperEntityResolver;
import com.ly.zmn48644.mybatis.parsing.XNode;
import com.ly.zmn48644.mybatis.parsing.XPathParser;
import com.ly.zmn48644.mybatis.session.Configuration;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

public class XMLMapperBuilder extends BaseBuilder {

    private final XPathParser parser;
    private final Map<String, XNode> sqlFragments;
    private final String resource;


    @Deprecated
    public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
        this(reader, configuration, resource, sqlFragments);
        // this.builderAssistant.setCurrentNamespace(namespace);
    }

    @Deprecated
    public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
        this(new XPathParser(reader, true, configuration.getVariables(), new XMLMapperEntityResolver()),
                configuration, resource, sqlFragments);
    }

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
        this(inputStream, configuration, resource, sqlFragments);
        // this.builderAssistant.setCurrentNamespace(namespace);
    }

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
        this(new XPathParser(inputStream, true, configuration.getVariables(), new XMLMapperEntityResolver()),
                configuration, resource, sqlFragments);
    }

    private XMLMapperBuilder(XPathParser parser, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
        super(configuration);
        //   this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
        this.parser = parser;
        this.sqlFragments = sqlFragments;
        this.resource = resource;
    }

    /**
     * XMLMapperBuilder类的核心方法
     */
    public void parse() {
        //首先判断 resource指向的资源是否被解析过
        if (!configuration.isResourceLoaded(resource)) {
            //
            configurationElement(parser.evalNode("/mapper"));
            configuration.addLoadedResource(resource);
            bindMapperForNamespace();
        }
        parsePendingResultMaps();
        parsePendingCacheRefs();
        parsePendingStatements();
    }

    private void bindMapperForNamespace() {
    }

    private void parsePendingCacheRefs() {
        
    }

    private void parsePendingStatements() {
    }

    private void parsePendingResultMaps() {
    }

    private void configurationElement(XNode xNode) {
    }
}
