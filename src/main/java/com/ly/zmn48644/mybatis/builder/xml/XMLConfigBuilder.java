
package com.ly.zmn48644.mybatis.builder.xml;


import com.ly.zmn48644.mybatis.builder.BaseBuilder;
import com.ly.zmn48644.mybatis.builder.BuilderException;
import com.ly.zmn48644.mybatis.datasource.DataSourceFactory;
import com.ly.zmn48644.mybatis.executor.ErrorContext;
import com.ly.zmn48644.mybatis.io.Resources;
import com.ly.zmn48644.mybatis.io.VFS;
import com.ly.zmn48644.mybatis.logging.Log;
import com.ly.zmn48644.mybatis.mapping.Environment;
import com.ly.zmn48644.mybatis.parsing.XNode;
import com.ly.zmn48644.mybatis.parsing.XPathParser;
import com.ly.zmn48644.mybatis.reflection.DefaultReflectorFactory;
import com.ly.zmn48644.mybatis.reflection.MetaClass;
import com.ly.zmn48644.mybatis.reflection.ReflectorFactory;
import com.ly.zmn48644.mybatis.reflection.factory.ObjectFactory;
import com.ly.zmn48644.mybatis.reflection.warpper.ObjectWrapperFactory;
import com.ly.zmn48644.mybatis.session.AutoMappingBehavior;
import com.ly.zmn48644.mybatis.session.Configuration;
import com.ly.zmn48644.mybatis.session.ExecutorType;
import com.ly.zmn48644.mybatis.session.LocalCacheScope;
import com.ly.zmn48644.mybatis.transaction.TransactionFactory;
import com.ly.zmn48644.mybatis.type.JdbcType;
import com.ly.zmn48644.mybatis.type.TypeHandler;


import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;


/**
 * 核心作用就是构建 Configuration 对象
 */
public class XMLConfigBuilder extends BaseBuilder {

    //是否调用过 parse .
    private boolean parsed;
    //配置文件解析器
    private final XPathParser parser;
    //启动环境,比如 测试环境,生产环境 等等 .
    private String environment;
    //反射器工厂类,此类中缓存了类的反射信息
    private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();

    public XMLConfigBuilder(Reader reader) {
        this(reader, null, null);
    }

    public XMLConfigBuilder(Reader reader, String environment) {
        this(reader, environment, null);
    }

    public XMLConfigBuilder(Reader reader, String environment, Properties props) {
        this(new XPathParser(reader, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    public XMLConfigBuilder(InputStream inputStream) {
        this(inputStream, null, null);
    }

    public XMLConfigBuilder(InputStream inputStream, String environment) {
        this(inputStream, environment, null);
    }

    public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    /**
     * 构造方法核心方法
     *
     * @param parser
     * @param environment
     * @param props
     */
    private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {

        //在这里创建了一个 空的  Configuration 对象,设置给父类的构造方法.
        super(new Configuration());
        //TODO 目前还不清楚作用
        ErrorContext.instance().resource("SQL Mapper Configuration");

        //设置外部配置数据
        this.configuration.setVariables(props);
        //将 parsed 这只为 false .
        this.parsed = false;
        //设置环境参数
        this.environment = environment;
        //设置 xml 解析器
        this.parser = parser;
    }

    /**
     * 调用 解析方法,解析xml文件返回 Configuration对象 \
     *
     * @return
     */
    public Configuration parse() {
        //如果已经解析过配置则抛出异常.
        if (parsed) {
            //每一个 XMLConfigBuilder 对象只能解析配置一次
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }
        //设置为已经解析过配置
        parsed = true;
        //调用解析方法,解析配置文件中的configuration 节点下的配置
        parseConfiguration(parser.evalNode("/configuration"));

        //返回解析后的  configuration 对象
        return configuration;
    }

    private void parseConfiguration(XNode root) {
        try {

            //解析配置文件中的 properties 元素节点
            //此方法没有返回值,并且是私有,方法里直接操作当前对象属性进行赋值.
            propertiesElement(root.evalNode("properties"));

            //解析配置文件中的 settings 元素, 使用 settingsAsProperties 方法返回一个 Properties 对象.
            //settings节点是 mybatis 的全局性 配置, 这里的配置回改变 mybatis 的运行时行为.
            Properties settings = settingsAsProperties(root.evalNode("settings"));


            //如果配置了,自定义的虚拟文件系统,则加载.
            loadCustomVfs(settings);

            //解析配置文件中的别名配置.
            typeAliasesElement(root.evalNode("typeAliases"));
            //TODO 临时注释 涉及到插件模块
            //解析插件模块
            //pluginElement(root.evalNode("plugins"));
            //解析 objectFactory 配置自定义的对象工厂.
            objectFactoryElement(root.evalNode("objectFactory"));

            //解析 objectWrapperFactory 配置自定义的.
            objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
            //解析 reflectorFactory 如果有自定义则替换默认的反射工厂
            reflectorFactoryElement(root.evalNode("reflectorFactory"));

            settingsElement(settings);

            //解析 环境 配置
            environmentsElement(root.evalNode("environments"));
            //TODO 临时注释
            //databaseIdProviderElement(root.evalNode("databaseIdProvider"));

            //解析自定义的类型处理器.
            typeHandlerElement(root.evalNode("typeHandlers"));
            //TODO 临时注释
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }


    /**
     * xml 中读取配置数据,转换为 Properties
     *
     * @param context
     * @return
     */
    private Properties settingsAsProperties(XNode context) {
        //如果没有解析到节点,也就是配置文件中没有配置相应的节点则,初始化一个空的 Properties 对象返回.
        if (context == null) {
            return new Properties();
        }
        //获取配置节点中的 配置数据.
        Properties props = context.getChildrenAsProperties();

        //这里获取到 Configuration 类的 MetaClass 信息
        MetaClass metaConfig = MetaClass.forClass(Configuration.class, localReflectorFactory);
        //检测 key 指定的属性 在 configuration 中有没有对应的 set 方法.
        for (Object key : props.keySet()) {
            if (!metaConfig.hasSetter(String.valueOf(key))) {
                throw new BuilderException("The setting " + key + " is not known.  Make sure you spelled it correctly (case sensitive).");
            }
        }
        return props;
    }

    /**
     * 加载自定义虚拟文件系统的配置.
     *
     * @param props
     * @throws ClassNotFoundException
     */
    private void loadCustomVfs(Properties props) throws ClassNotFoundException {
        //从Properties 获取 自定义的虚拟文件系统配置.
        String value = props.getProperty("vfsImpl");
        //如果配置了 自定义虚拟文件系统配置
        if (value != null) {
            //由于可以通过 逗号 分割配置多个,所以需要转换成数组.
            String[] clazzes = value.split(",");
            for (String clazz : clazzes) {
                //如果配置的字符串不是 empty 的字符串
                if (!clazz.isEmpty()) {
                    //加载字符串指定的class .
                    Class<? extends VFS> vfsImpl = (Class<? extends VFS>) Resources.classForName(clazz);
                    //将加载到的class设置到 configuration 对象中.
                    configuration.setVfsImpl(vfsImpl);
                }
            }
        }
    }

    /**
     * 解析别名配置
     *
     * @param parent
     */
    private void typeAliasesElement(XNode parent) {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                //如果配置中子元素的名字为  package 说明是指定包下面所有的类,属于批量的映射
                if ("package".equals(child.getName())) {
                    //获取别名映射包名
                    String typeAliasPackage = child.getStringAttribute("name");
                    //registerAliases 用于注册包下面的所有类.
                    configuration.getTypeAliasRegistry().registerAliases(typeAliasPackage);
                } else {
                    //如果不是通过包名指定的
                    String alias = child.getStringAttribute("alias");
                    String type = child.getStringAttribute("type");
                    try {
                        //通过类加载器将字符串,加载为类.
                        Class<?> clazz = Resources.classForName(type);
                        //判断有没有 设置 alias 属性
                        if (alias == null) {
                            //如果没有设置 alias 属性 则调用下面方法,只传入class,这样使用的是默认的别名
                            typeAliasRegistry.registerAlias(clazz);
                        } else {
                            //如果设置了 alias 属性说明不要使用 默认的别名.
                            typeAliasRegistry.registerAlias(alias, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + e, e);
                    }
                }
            }
        }
    }

//  private void pluginElement(XNode parent) throws Exception {
//    if (parent != null) {
//      for (XNode child : parent.getChildren()) {
//        String interceptor = child.getStringAttribute("interceptor");
//        Properties properties = child.getChildrenAsProperties();
//        Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();
//        interceptorInstance.setProperties(properties);
//        configuration.addInterceptor(interceptorInstance);
//      }
//    }
//  }

    private void objectFactoryElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties properties = context.getChildrenAsProperties();
            ObjectFactory factory = (ObjectFactory) resolveClass(type).newInstance();
            factory.setProperties(properties);
            configuration.setObjectFactory(factory);
        }
    }

    private void objectWrapperFactoryElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            ObjectWrapperFactory factory = (ObjectWrapperFactory) resolveClass(type).newInstance();
            configuration.setObjectWrapperFactory(factory);
        }
    }

    private void reflectorFactoryElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            ReflectorFactory factory = (ReflectorFactory) resolveClass(type).newInstance();
            configuration.setReflectorFactory(factory);
        }
    }

    private void propertiesElement(XNode context) throws Exception {
        if (context != null) {
            //解析传入元素中的 name和value 属性,并且设置到 properties 对象中.
            Properties defaults = context.getChildrenAsProperties();
            //解析 resource 和 url 确定配置文件的位置
            String resource = context.getStringAttribute("resource");
            String url = context.getStringAttribute("url");

            //如果 resource 和 url 均没有指定 抛出异常
            if (resource != null && url != null) {
                throw new BuilderException("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
            }
            if (resource != null) {
                //如果 配置了 resource 则加载所有配置
                defaults.putAll(Resources.getResourceAsProperties(resource));
            } else if (url != null) {
                //如果 配置了 url 则根据 url 加载配置
                defaults.putAll(Resources.getUrlAsProperties(url));
            }
            Properties vars = configuration.getVariables();
            if (vars != null) {
                //将 configuration 配置中的数据 放入 defaults中,合并两部分的配置
                defaults.putAll(vars);
            }
            //更新 XpathParser 和 Configuration 中的配置数据
            parser.setVariables(defaults);
            configuration.setVariables(defaults);
        }
    }

    private void settingsElement(Properties props) throws Exception {
        //TODO 此配置需要写代码验证
        //结果集映射配置, 默认配置为 PARTIAL.
        configuration.setAutoMappingBehavior(AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior", "PARTIAL")));

        //TODO 临时注释 涉及到 mapping 模块
        //configuration.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.valueOf(props.getProperty("autoMappingUnknownColumnBehavior", "NONE")));

        //全局性的配置是否启用缓存
        configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));

        //TODO 临时注释 涉及到 executor 模块
        //configuration.setProxyFactory((ProxyFactory) createInstance(props.getProperty("proxyFactory")));

        //全局配置是否启用懒加载
        configuration.setLazyLoadingEnabled(booleanValueOf(props.getProperty("lazyLoadingEnabled"), false));

        //从字面意思来说是 积极懒加载 配置.
        //如果配置为 true , 如果调用了一个懒加载的属性, 其他懒加载的属性也会触发加载, 默认为 false.
        configuration.setAggressiveLazyLoading(booleanValueOf(props.getProperty("aggressiveLazyLoading"), false));

        //全局配置 是否允许单一语句返回多结果集
        configuration.setMultipleResultSetsEnabled(booleanValueOf(props.getProperty("multipleResultSetsEnabled"), true));

        //是否使用列别名替换列名
        configuration.setUseColumnLabel(booleanValueOf(props.getProperty("useColumnLabel"), true));

        //全局配置 是否使用自动生成的主键 , 也可以在 insert 语句总专门指定.
        configuration.setUseGeneratedKeys(booleanValueOf(props.getProperty("useGeneratedKeys"), false));


        //TODO 暂时不深入
        //全局配置默认执行器
        configuration.setDefaultExecutorType(ExecutorType.valueOf(props.getProperty("defaultExecutorType", "SIMPLE")));

        //全局配置 设置超时时间，它决定驱动等待数据库响应的秒数
        configuration.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"), null));

        //TODO 暂时不深入
        //和数据库读取数据的模式有关
        configuration.setDefaultFetchSize(integerValueOf(props.getProperty("defaultFetchSize"), null));

        //启用 下换线命名 到驼峰命名 属性映射,默认不启用.
        configuration.setMapUnderscoreToCamelCase(booleanValueOf(props.getProperty("mapUnderscoreToCamelCase"), false));

        //是否允许在嵌套语句中使用分页
        configuration.setSafeRowBoundsEnabled(booleanValueOf(props.getProperty("safeRowBoundsEnabled"), false));


        //TODO 暂时不深入
        //本地缓存范围
        configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope", "SESSION")));

        //当没有为参数提供特定的 JDBC 类型时，为空值指定 JDBC 类型。
        configuration.setJdbcTypeForNull(JdbcType.valueOf(props.getProperty("jdbcTypeForNull", "OTHER")));

        //指定触发加载的方法
        configuration.setLazyLoadTriggerMethods(stringSetValueOf(props.getProperty("lazyLoadTriggerMethods"), "equals,clone,hashCode,toString"));

        //TODO 暂时不深入
        configuration.setSafeResultHandlerEnabled(booleanValueOf(props.getProperty("safeResultHandlerEnabled"), true));


        //临时注释 涉及到 scripting 模块 暂不深入
        //指定所使用的语言默认为动态SQL生成
        //configuration.setDefaultScriptingLanguage(resolveClass(props.getProperty("defaultScriptingLanguage")));

        @SuppressWarnings("unchecked")
        //解析默认枚举处理器
                Class<? extends TypeHandler> typeHandler = (Class<? extends TypeHandler>) resolveClass(props.getProperty("defaultEnumTypeHandler"));
        configuration.setDefaultEnumTypeHandler(typeHandler);

        //当结果集中含有Null值时是否执行映射对象的setter或者Map对象的put方法。
        configuration.setCallSettersOnNulls(booleanValueOf(props.getProperty("callSettersOnNulls"), false));

        //是否使用实际参数名
        configuration.setUseActualParamName(booleanValueOf(props.getProperty("useActualParamName"), true));

        //如何处理空行配置
        configuration.setReturnInstanceForEmptyRow(booleanValueOf(props.getProperty("returnInstanceForEmptyRow"), false));
        //指定 MyBatis 增加到日志名称的前缀。
        configuration.setLogPrefix(props.getProperty("logPrefix"));

        //指定日志实现
        @SuppressWarnings("unchecked")
        Class<? extends Log> logImpl = (Class<? extends Log>) resolveClass(props.getProperty("logImpl"));
        configuration.setLogImpl(logImpl);

        // configuration 工厂类配置
        configuration.setConfigurationFactory(resolveClass(props.getProperty("configurationFactory")));
    }

    /**
     * 解析
     *
     * @param context
     * @throws Exception
     */
    private void environmentsElement(XNode context) throws Exception {
        if (context != null) {
            if (environment == null) {
                //获取 default 环境配置
                environment = context.getStringAttribute("default");
            }
            for (XNode child : context.getChildren()) {
                String id = child.getStringAttribute("id");
                if (isSpecifiedEnvironment(id)) {
                    TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
                    DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
                    DataSource dataSource = dsFactory.getDataSource();
                    Environment.Builder environmentBuilder = new Environment.Builder(id)
                            .transactionFactory(txFactory)
                            .dataSource(dataSource);
                    configuration.setEnvironment(environmentBuilder.build());
                }
            }
        }
    }

//  private void databaseIdProviderElement(XNode context) throws Exception {
//    DatabaseIdProvider databaseIdProvider = null;
//    if (context != null) {
//      String type = context.getStringAttribute("type");
//      // awful patch to keep backward compatibility
//      if ("VENDOR".equals(type)) {
//          type = "DB_VENDOR";
//      }
//      Properties properties = context.getChildrenAsProperties();
//      databaseIdProvider = (DatabaseIdProvider) resolveClass(type).newInstance();
//      databaseIdProvider.setProperties(properties);
//    }
//    Environment environment = configuration.getEnvironment();
//    if (environment != null && databaseIdProvider != null) {
//      String databaseId = databaseIdProvider.getDatabaseId(environment.getDataSource());
//      configuration.setDatabaseId(databaseId);
//    }
//  }

    private TransactionFactory transactionManagerElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            TransactionFactory factory = (TransactionFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a TransactionFactory.");
    }

    private DataSourceFactory dataSourceElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            DataSourceFactory factory = (DataSourceFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a DataSourceFactory.");
    }

    private void typeHandlerElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                if ("package".equals(child.getName())) {
                    String typeHandlerPackage = child.getStringAttribute("name");
                    typeHandlerRegistry.register(typeHandlerPackage);
                } else {
                    String javaTypeName = child.getStringAttribute("javaType");
                    String jdbcTypeName = child.getStringAttribute("jdbcType");
                    String handlerTypeName = child.getStringAttribute("handler");
                    Class<?> javaTypeClass = resolveClass(javaTypeName);
                    JdbcType jdbcType = resolveJdbcType(jdbcTypeName);
                    Class<?> typeHandlerClass = resolveClass(handlerTypeName);
                    if (javaTypeClass != null) {
                        if (jdbcType == null) {
                            typeHandlerRegistry.register(javaTypeClass, typeHandlerClass);
                        } else {
                            typeHandlerRegistry.register(javaTypeClass, jdbcType, typeHandlerClass);
                        }
                    } else {
                        typeHandlerRegistry.register(typeHandlerClass);
                    }
                }
            }
        }
    }

    private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                if ("package".equals(child.getName())) {
                    String mapperPackage = child.getStringAttribute("name");
                    //TODO 注释
                    //configuration.addMappers(mapperPackage);
                } else {
                    String resource = child.getStringAttribute("resource");
                    String url = child.getStringAttribute("url");
                    String mapperClass = child.getStringAttribute("class");
                    if (resource != null && url == null && mapperClass == null) {
                        ErrorContext.instance().resource(resource);
                        InputStream inputStream = Resources.getResourceAsStream(resource);
                        //TODO 注释
//            XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
//            mapperParser.parse();
                    } else if (resource == null && url != null && mapperClass == null) {
                        ErrorContext.instance().resource(url);
                        InputStream inputStream = Resources.getUrlAsStream(url);
                        //TODO 注释
//            XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
//            mapperParser.parse();
                    } else if (resource == null && url == null && mapperClass != null) {
                        Class<?> mapperInterface = Resources.classForName(mapperClass);
                        //TODO 注释
                        //configuration.addMapper(mapperInterface);
                    } else {
                        throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                    }
                }
            }
        }
    }

    private boolean isSpecifiedEnvironment(String id) {
        if (environment == null) {
            throw new BuilderException("No environment specified.");
        } else if (id == null) {
            throw new BuilderException("Environment requires an id attribute.");
        } else if (environment.equals(id)) {
            return true;
        }
        return false;
    }

}
