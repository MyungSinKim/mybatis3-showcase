package com.ly.zmn48644.mybatis.builder;

import com.ly.zmn48644.mybatis.builder.xml.XMLMapperEntityResolver;
import com.ly.zmn48644.mybatis.cache.Cache;
import com.ly.zmn48644.mybatis.parsing.XNode;
import com.ly.zmn48644.mybatis.parsing.XPathParser;
import com.ly.zmn48644.mybatis.session.Configuration;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

public class XMLMapperBuilder extends BaseBuilder {

    private final XPathParser parser;
    private final Map<String, XNode> sqlFragments;
    private final MapperBuilderAssistant builderAssistant;
    private final String resource;


    @Deprecated
    public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
        this(reader, configuration, resource, sqlFragments);
        this.builderAssistant.setCurrentNamespace(namespace);
    }

    @Deprecated
    public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
        this(new XPathParser(reader, true, configuration.getVariables(), new XMLMapperEntityResolver()),
                configuration, resource, sqlFragments);
    }

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
        this(inputStream, configuration, resource, sqlFragments);
        this.builderAssistant.setCurrentNamespace(namespace);
    }

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
        this(new XPathParser(inputStream, true, configuration.getVariables(), new XMLMapperEntityResolver()),
                configuration, resource, sqlFragments);
    }

    private XMLMapperBuilder(XPathParser parser, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
        super(configuration);
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
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
            //解析 mapper 标签
            configurationElement(parser.evalNode("/mapper"));
            configuration.addLoadedResource(resource);
            bindMapperForNamespace();
        }
        parsePendingResultMaps();
        parsePendingCacheRefs();
        parsePendingStatements();
    }

    /**
     * 此方法每个资源只会执行一次
     *
     * @param context
     */
    private void configurationElement(XNode context) {
        try {
            String namespace = context.getStringAttribute("namespace");
            if (namespace == null || namespace.equals("")) {
                throw new BuilderException("Mapper's namespace cannot be empty");
            }

            builderAssistant.setCurrentNamespace(namespace);
            cacheRefElement(context.evalNode("cache-ref"));

            cacheElement(context.evalNode("cache"));

//            parameterMapElement(context.evalNodes("/mapper/parameterMap"));
//            resultMapElements(context.evalNodes("/mapper/resultMap"));
//            sqlElement(context.evalNodes("/mapper/sql"));
//            buildStatementFromContext(context.evalNodes("select|insert|update|delete"));

        } catch (Exception e) {
            throw new BuilderException("Error parsing Mapper XML. Cause: " + e, e);
        }
    }

    private void cacheRefElement(XNode context) {
        if (context != null) {
            configuration.addCacheRef(builderAssistant.getCurrentNamespace(), context.getStringAttribute("namespace"));
            CacheRefResolver cacheRefResolver = new CacheRefResolver(builderAssistant, context.getStringAttribute("namespace"));
            try {
                cacheRefResolver.resolveCacheRef();
            } catch (IncompleteElementException e) {
                configuration.addIncompleteCacheRef(cacheRefResolver);
            }
        }
    }

    private void cacheElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type", "PERPETUAL");
            Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);
            String eviction = context.getStringAttribute("eviction", "LRU");
            Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);
            Long flushInterval = context.getLongAttribute("flushInterval");
            Integer size = context.getIntAttribute("size");
            boolean readWrite = !context.getBooleanAttribute("readOnly", false);
            boolean blocking = context.getBooleanAttribute("blocking", false);
            Properties props = context.getChildrenAsProperties();
            builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
        }
    }

    private void bindMapperForNamespace() {
    }

    private void parsePendingCacheRefs() {

    }

    private void parsePendingStatements() {
    }

    private void parsePendingResultMaps() {
    }

}
