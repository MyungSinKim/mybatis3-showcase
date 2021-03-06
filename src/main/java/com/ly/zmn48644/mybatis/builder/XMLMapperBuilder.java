package com.ly.zmn48644.mybatis.builder;

import com.ly.zmn48644.mybatis.builder.xml.XMLMapperEntityResolver;
import com.ly.zmn48644.mybatis.builder.xml.XMLStatementBuilder;
import com.ly.zmn48644.mybatis.cache.Cache;
import com.ly.zmn48644.mybatis.executor.ErrorContext;
import com.ly.zmn48644.mybatis.io.Resources;
import com.ly.zmn48644.mybatis.mapping.*;
import com.ly.zmn48644.mybatis.parsing.XNode;
import com.ly.zmn48644.mybatis.parsing.XPathParser;
import com.ly.zmn48644.mybatis.session.Configuration;
import com.ly.zmn48644.mybatis.type.JdbcType;
import com.ly.zmn48644.mybatis.type.TypeHandler;

import java.io.InputStream;
import java.io.Reader;
import java.util.*;

public class XMLMapperBuilder extends BaseBuilder {
    //获取xml配置中的指定节点的数据
    private final XPathParser parser;
    //SQL片段集合
    private final Map<String, XNode> sqlFragments;
    //辅助当前类完成具体节点的解析
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
            //TODO 关键方法!
            configurationElement(parser.evalNode("/mapper"));
            configuration.addLoadedResource(resource);
            //注册 mapper
            bindMapperForNamespace();
        }
        //处理解析
        parsePendingResultMaps();
        parsePendingCacheRefs();
        parsePendingStatements();
    }

    /**
     * 完成 映射配置文件与对应Mapper接口的绑定
     */
    private void bindMapperForNamespace() {
        //获取命名空间
        String namespace = builderAssistant.getCurrentNamespace();
        if (namespace != null) {
            Class<?> boundType = null;
            try {
                //获取命名空间指向的Mapper接口的class对象
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


    /**
     * 处理configurationElement方法中解析失败的<cache-ref>节点
     * 在解析时是按照上到下顺序解析的,因此会出现 在解析一个节点时 引用了在 之后定义的节点
     * 这样就造成了解析此节点的失败抛出异常,可以称之为未完成解析,程序的处理逻辑是将其暂存在集合中
     * 最后在处理这些未完成的节点解析.
     */
    private void parsePendingCacheRefs() {
        /**
         *  获取未完成节点结合
         *  CacheRefResolver 是一个封装类
         */
        Collection<CacheRefResolver> incompleteCacheRefs = configuration.getIncompleteCacheRefs();
        synchronized (incompleteCacheRefs) {
            Iterator<CacheRefResolver> iter = incompleteCacheRefs.iterator();
            while (iter.hasNext()) {
                try {
                    //重新调用 useCacheRef 方法解析
                    iter.next().resolveCacheRef();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // Cache ref is still missing a resource...
                    //这里不做处理
                }
            }
        }
    }

    /**
     * 处理configurationElement方法中解析失败的 SQL 语句节点
     */
    private void parsePendingStatements() {
        Collection<XMLStatementBuilder> incompleteStatements = configuration.getIncompleteStatements();
        synchronized (incompleteStatements) {
            Iterator<XMLStatementBuilder> iter = incompleteStatements.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().parseStatementNode();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // Statement is still missing a resource...
                }
            }
        }
    }

    /**
     * 处理configurationElement方法中解析失败的<resultMap>节点
     */
    private void parsePendingResultMaps() {
        Collection<ResultMapResolver> incompleteResultMaps = configuration.getIncompleteResultMaps();
        synchronized (incompleteResultMaps) {
            Iterator<ResultMapResolver> iter = incompleteResultMaps.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().resolve();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // ResultMap is still missing a resource...
                }
            }
        }
    }


    /**
     * xml解析的核心逻辑
     * 参考文档 http://www.mybatis.org/mybatis-3/zh/sqlmap-xml.html
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
            //解析缓存引用配置
            cacheRefElement(context.evalNode("cache-ref"));
            //解析缓存配置
            cacheElement(context.evalNode("cache"));
            //parameterMap – 已废弃！老式风格的参数映射。内联参数是首选,这个元素可能在将来被移除，这里不会记录。
            parameterMapElement(context.evalNodes("/mapper/parameterMap"));

            //解析结果集映射配置
            resultMapElements(context.evalNodes("/mapper/resultMap"));

            //解析SQL标签元素
            sqlElement(context.evalNodes("/mapper/sql"));
            //解析 statement
            buildStatementFromContext(context.evalNodes("select|insert|update|delete"));

        } catch (Exception e) {
            throw new BuilderException("Error parsing Mapper XML. Cause: " + e, e);
        }
    }

    /**
     * 解析映射配置文件中的 resultMap 标签
     *
     * @param list
     * @throws Exception
     */
    private void resultMapElements(List<XNode> list) throws Exception {
        //可能存在多个 resultMap 标签
        for (XNode resultMapNode : list) {
            try {
                resultMapElement(resultMapNode);
            } catch (IncompleteElementException e) {
                // ignore, it will be retried
            }
        }
    }

    private ResultMap resultMapElement(XNode resultMapNode) throws Exception {
        return resultMapElement(resultMapNode, Collections.<ResultMapping>emptyList());
    }

    /**
     * 解析 resultMap 节点
     * 参考官方文档:http://www.mybatis.org/mybatis-3/zh/sqlmap-xml.html#Result_Maps
     *
     * @param resultMapNode
     * @param additionalResultMappings
     * @return
     * @throws Exception
     */
    private ResultMap resultMapElement(XNode resultMapNode, List<ResultMapping> additionalResultMappings) throws Exception {
        ErrorContext.instance().activity("processing " + resultMapNode.getValueBasedIdentifier());

        /**
         * 获取节点 id 属性,如果id属性没有设置则 调用 resultMapNode.getValueBasedIdentifier()生成一个id
         */
        String id = resultMapNode.getStringAttribute("id", resultMapNode.getValueBasedIdentifier());
        /**
         * 按照  type -> ofType -> resultType -> javaType 的优先级获取配置
         * 结果集将会被映射成 type指定的对象,注意默认值
         */
        String type = resultMapNode.getStringAttribute("type", resultMapNode.getStringAttribute("ofType", resultMapNode.getStringAttribute("resultType", resultMapNode.getStringAttribute("javaType"))));

        /**
         * 获取 extends 属性配置,此属性决定了当前resultMap的继承关系
         */
        String extend = resultMapNode.getStringAttribute("extends");

        /**
         * 获取 autoMapping 属性配置,自动查找与列名 相同的属性名,调用set方法设置值,如果为false则需要在<resultMap>节点中明确指定映射关系
         * 才会调用set方法设置值
         */
        Boolean autoMapping = resultMapNode.getBooleanAttribute("autoMapping");

        /**
         * 解析type类型
         */
        Class<?> typeClass = resolveClass(type);

        /**
         * 鉴别器在实际项目中用的场景比较少
         */
        Discriminator discriminator = null;

        List<ResultMapping> resultMappings = new ArrayList<ResultMapping>();

        resultMappings.addAll(additionalResultMappings);

        //获取所有子节点
        List<XNode> resultChildren = resultMapNode.getChildren();
        //开始遍历所有子节点
        for (XNode resultChild : resultChildren) {
            //判断当前子节点是否是 constructor 标签
            if ("constructor".equals(resultChild.getName())) {
                //处理constructor标签内子元素
                processConstructorElement(resultChild, typeClass, resultMappings);

            } else if ("discriminator".equals(resultChild.getName())) {
                //处理鉴别器节点的子元素
                discriminator = processDiscriminatorElement(resultChild, typeClass, resultMappings);
            } else {
                //处理普通映射节点
                List<ResultFlag> flags = new ArrayList<ResultFlag>();
                if ("id".equals(resultChild.getName())) {
                    //如果是id节点
                    flags.add(ResultFlag.ID);
                }
                resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
            }
        }
        ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, extend, discriminator, resultMappings, autoMapping);
        try {
            //调用 resolve 方法将 resultMap 添加到全局配置对象中区
            return resultMapResolver.resolve();
        } catch (IncompleteElementException e) {
            //如果抛出异常则 添加到 需要重新处理的resultMap集合中去
            configuration.addIncompleteResultMap(resultMapResolver);
            throw e;
        }
    }


    private void parameterMapElement(List<XNode> list) throws Exception {
        for (XNode parameterMapNode : list) {
            String id = parameterMapNode.getStringAttribute("id");
            String type = parameterMapNode.getStringAttribute("type");
            Class<?> parameterClass = resolveClass(type);
            List<XNode> parameterNodes = parameterMapNode.evalNodes("parameter");
            List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
            for (XNode parameterNode : parameterNodes) {
                String property = parameterNode.getStringAttribute("property");
                String javaType = parameterNode.getStringAttribute("javaType");
                String jdbcType = parameterNode.getStringAttribute("jdbcType");
                String resultMap = parameterNode.getStringAttribute("resultMap");
                String mode = parameterNode.getStringAttribute("mode");
                String typeHandler = parameterNode.getStringAttribute("typeHandler");
                Integer numericScale = parameterNode.getIntAttribute("numericScale");
                ParameterMode modeEnum = resolveParameterMode(mode);
                Class<?> javaTypeClass = resolveClass(javaType);
                JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
                @SuppressWarnings("unchecked")
                Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
                ParameterMapping parameterMapping = builderAssistant.buildParameterMapping(parameterClass, property, javaTypeClass, jdbcTypeEnum, resultMap, modeEnum, typeHandlerClass, numericScale);
                parameterMappings.add(parameterMapping);
            }
            builderAssistant.addParameterMap(id, parameterClass, parameterMappings);
        }
    }

    /**
     * 解析cache-ref节点
     *
     * @param context
     */
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

    /**
     * 解析cache节点
     *
     * @param context
     * @throws Exception
     */
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

    /**
     * 处理 constructor 标签元素
     *
     * @param resultChild
     * @param resultType
     * @param resultMappings
     * @throws Exception
     */
    private void processConstructorElement(XNode resultChild, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {
        List<XNode> argChildren = resultChild.getChildren();
        for (XNode argChild : argChildren) {
            List<ResultFlag> flags = new ArrayList<ResultFlag>();
            flags.add(ResultFlag.CONSTRUCTOR);
            if ("idArg".equals(argChild.getName())) {
                flags.add(ResultFlag.ID);
            }
            resultMappings.add(buildResultMappingFromContext(argChild, resultType, flags));
        }
    }

    private Discriminator processDiscriminatorElement(XNode context, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {
        String column = context.getStringAttribute("column");
        String javaType = context.getStringAttribute("javaType");
        String jdbcType = context.getStringAttribute("jdbcType");
        String typeHandler = context.getStringAttribute("typeHandler");
        Class<?> javaTypeClass = resolveClass(javaType);
        @SuppressWarnings("unchecked")
        Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
        Map<String, String> discriminatorMap = new HashMap<String, String>();
        for (XNode caseChild : context.getChildren()) {
            String value = caseChild.getStringAttribute("value");
            String resultMap = caseChild.getStringAttribute("resultMap", processNestedResultMappings(caseChild, resultMappings));
            discriminatorMap.put(value, resultMap);
        }
        return builderAssistant.buildDiscriminator(resultType, column, javaTypeClass, jdbcTypeEnum, typeHandlerClass, discriminatorMap);
    }

    /**
     * 构建 ResultMapping 方法
     * 此方法中调用 builderAssistant 进行真正的创建
     * 这个方法中解析拼装参数
     *
     * @param context
     * @param resultType
     * @param flags
     * @return
     * @throws Exception
     */
    private ResultMapping buildResultMappingFromContext(XNode context, Class<?> resultType, List<ResultFlag> flags) throws Exception {
        String property;
        if (flags.contains(ResultFlag.CONSTRUCTOR)) {
            property = context.getStringAttribute("name");
        } else {
            property = context.getStringAttribute("property");
        }
        String column = context.getStringAttribute("column");
        String javaType = context.getStringAttribute("javaType");
        String jdbcType = context.getStringAttribute("jdbcType");
        String nestedSelect = context.getStringAttribute("select");
        String nestedResultMap = context.getStringAttribute("resultMap",
                processNestedResultMappings(context, Collections.<ResultMapping>emptyList()));
        String notNullColumn = context.getStringAttribute("notNullColumn");
        String columnPrefix = context.getStringAttribute("columnPrefix");
        String typeHandler = context.getStringAttribute("typeHandler");
        String resultSet = context.getStringAttribute("resultSet");
        String foreignColumn = context.getStringAttribute("foreignColumn");
        boolean lazy = "lazy".equals(context.getStringAttribute("fetchType", configuration.isLazyLoadingEnabled() ? "lazy" : "eager"));
        Class<?> javaTypeClass = resolveClass(javaType);
        @SuppressWarnings("unchecked")
        Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);

        //调用 建造者助理类 完成具体的创建工作.
        return builderAssistant.buildResultMapping(resultType, property, column, javaTypeClass, jdbcTypeEnum, nestedSelect, nestedResultMap, notNullColumn, columnPrefix, typeHandlerClass, flags, resultSet, foreignColumn, lazy);
    }

    /**
     * 这里处理嵌套的映射
     * @param context
     * @param resultMappings
     * @return
     * @throws Exception
     */
    private String processNestedResultMappings(XNode context, List<ResultMapping> resultMappings) throws Exception {
        if ("association".equals(context.getName())
                || "collection".equals(context.getName())
                || "case".equals(context.getName())) {
            if (context.getStringAttribute("select") == null) {
                ResultMap resultMap = resultMapElement(context, resultMappings);
                return resultMap.getId();
            }
        }
        return null;
    }

    /**
     * 解析SQL节点配置
     *
     * @param list
     * @throws Exception
     */
    private void sqlElement(List<XNode> list) throws Exception {
        if (configuration.getDatabaseId() != null) {
            sqlElement(list, configuration.getDatabaseId());
        }
        sqlElement(list, null);
    }

    /**
     * 解析SQL节点配置
     *
     * @param list
     * @param requiredDatabaseId
     * @throws Exception
     */
    private void sqlElement(List<XNode> list, String requiredDatabaseId) throws Exception {
        for (XNode context : list) {
            String databaseId = context.getStringAttribute("databaseId");
            String id = context.getStringAttribute("id");
            id = builderAssistant.applyCurrentNamespace(id, false);
            if (databaseIdMatchesCurrent(id, databaseId, requiredDatabaseId)) {
                sqlFragments.put(id, context);
            }
        }
    }

    private boolean databaseIdMatchesCurrent(String id, String databaseId, String requiredDatabaseId) {
        if (requiredDatabaseId != null) {
            if (!requiredDatabaseId.equals(databaseId)) {
                return false;
            }
        } else {
            if (databaseId != null) {
                return false;
            }
            // skip this fragment if there is a previous one with a not null databaseId
            if (this.sqlFragments.containsKey(id)) {
                XNode context = this.sqlFragments.get(id);
                if (context.getStringAttribute("databaseId") != null) {
                    return false;
                }
            }
        }
        return true;
    }

    private void buildStatementFromContext(List<XNode> list) {
        if (configuration.getDatabaseId() != null) {
            buildStatementFromContext(list, configuration.getDatabaseId());
        }
        buildStatementFromContext(list, null);
    }

    private void buildStatementFromContext(List<XNode> list, String requiredDatabaseId) {
        for (XNode context : list) {
            final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, context, requiredDatabaseId);
            try {
                statementParser.parseStatementNode();
            } catch (IncompleteElementException e) {
                configuration.addIncompleteStatement(statementParser);
            }
        }
    }
}
