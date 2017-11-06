package com.ly.zmn48644.mybatis.builder;

import com.ly.zmn48644.mybatis.builder.xml.XMLMapperEntityResolver;
import com.ly.zmn48644.mybatis.cache.Cache;
import com.ly.zmn48644.mybatis.io.Resources;
import com.ly.zmn48644.mybatis.parsing.XNode;
import com.ly.zmn48644.mybatis.parsing.XPathParser;
import com.ly.zmn48644.mybatis.session.Configuration;

import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
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

    /**
     * 此构造方法 在 MapperAnnotationBuilder.loadXmlResource 方法中调用
     * 解析完接口中的注解后,去读取接口对应的XML文件.mybatis支持注解和xml两种方式配置sql,并且允许这两种形式同时存在.
     *
     * @param inputStream
     * @param configuration
     * @param resource
     * @param sqlFragments
     * @param namespace
     */
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
     * 根据命名空间绑定mapper
     */
    private void bindMapperForNamespace() {
        String namespace = builderAssistant.getCurrentNamespace();
        if (namespace != null) {
            Class<?> boundType = null;
            try {
                boundType = Resources.classForName(namespace);
            } catch (ClassNotFoundException e) {
                //ignore, bound type is not required
            }
            if (boundType != null) {
                //判断是否绑定过
                if (!configuration.hasMapper(boundType)) {
                    // Spring may not know the real resource name so we set a flag
                    // to prevent loading again this resource from the mapper interface
                    // look at MapperAnnotationBuilder#loadXmlResource

                    //namespace 添加到 configuration中的loadedResources集合中
                    configuration.addLoadedResource("namespace:" + namespace);
                    //将 namespace 所指向的接口 加入到 configuration中 mapperRegistry(映射器注册中心) 的knownMappers集合中去.
                    configuration.addMapper(boundType);
                }
            }
        }
    }

    private void parsePendingCacheRefs() {

    }

    private void parsePendingStatements() {
    }

    private void parsePendingResultMaps() {

    }

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


}
