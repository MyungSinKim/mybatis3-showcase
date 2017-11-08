
package com.ly.zmn48644.mybatis.builder.xml;


import com.ly.zmn48644.mybatis.builder.BaseBuilder;
import com.ly.zmn48644.mybatis.builder.BuilderException;
import com.ly.zmn48644.mybatis.builder.XMLMapperBuilder;
import com.ly.zmn48644.mybatis.datasource.DataSourceFactory;
import com.ly.zmn48644.mybatis.executor.ErrorContext;
import com.ly.zmn48644.mybatis.io.Resources;
import com.ly.zmn48644.mybatis.io.VFS;
import com.ly.zmn48644.mybatis.logging.Log;
import com.ly.zmn48644.mybatis.mapping.DatabaseIdProvider;
import com.ly.zmn48644.mybatis.mapping.Environment;
import com.ly.zmn48644.mybatis.parsing.XNode;
import com.ly.zmn48644.mybatis.parsing.XPathParser;
import com.ly.zmn48644.mybatis.reflection.DefaultReflectorFactory;
import com.ly.zmn48644.mybatis.reflection.MetaClass;
import com.ly.zmn48644.mybatis.reflection.ReflectorFactory;
import com.ly.zmn48644.mybatis.reflection.factory.ObjectFactory;
import com.ly.zmn48644.mybatis.reflection.warpper.ObjectWrapperFactory;
import com.ly.zmn48644.mybatis.session.*;
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
     * 调用 解析方法,解析xml文件返回 Configuration对象
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
        //此方法包含了所有的配置初始化流程
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

            //解析 启动配置 元素
            settingsElement(settings);

            //解析 环境 配置
            environmentsElement(root.evalNode("environments"));
            //解析 databaseIdProvider 配置
            databaseIdProviderElement(root.evalNode("databaseIdProvider"));

            //解析自定义的类型处理器.
            typeHandlerElement(root.evalNode("typeHandlers"));
            //解析 mapper.xml 文件
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }

    /**
     * 解析 databaseIdProvider 配置
     *
     * @param context
     * @throws Exception
     */
    private void databaseIdProviderElement(XNode context) throws Exception {
        DatabaseIdProvider databaseIdProvider = null;
        if (context != null) {
            String type = context.getStringAttribute("type");
            // awful patch to keep backward compatibility
            if ("VENDOR".equals(type)) {
                type = "DB_VENDOR";
            }
            Properties properties = context.getChildrenAsProperties();
            databaseIdProvider = (DatabaseIdProvider) resolveClass(type).newInstance();
            databaseIdProvider.setProperties(properties);
        }
        Environment environment = configuration.getEnvironment();
        if (environment != null && databaseIdProvider != null) {
            String databaseId = databaseIdProvider.getDatabaseId(environment.getDataSource());

            configuration.setDatabaseId(databaseId);
        }
    }

    /**
     * xml 中读取配置数据,转换为 Properties
     * 读取 settings 标签的配置数据.
     * 配置官方文档地址:http://www.mybatis.org/mybatis-3/zh/configuration.html#settings
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
        // settings 中的配置是 会设置给(configuration)的因此必须判断有set方法
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
     * 参考官方文档:http://www.mybatis.org/mybatis-3/zh/configuration.html#typeAliases
     * 别名配置是支持两种方式
     * 第一 通过typeAlias标签一个一个配置
     * 第二 通过package 配置一个包名,会自动注册包中所有的类
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

    /**
     * 配置文件中的 objectFactory 标签用来指定一个 objectFactory对象实现会覆盖默认的实现
     * 在标签内部也可以设置一些参数传给自定义的 objectFactory实现
     * 默认的对象工厂需要做的仅仅是实例化目标类，要么通过默认构造方法，要么在参数映射存在的时候通过参数构造方法来实例化.
     * 参考官方文档:http://www.mybatis.org/mybatis-3/zh/configuration.html#objectFactory
     *
     * @param context
     * @throws Exception
     */
    private void objectFactoryElement(XNode context) throws Exception {
        if (context != null) {
            //获取自定义的 objectFactory
            String type = context.getStringAttribute("type");
            //获取自定义对象工厂配置
            Properties properties = context.getChildrenAsProperties();

            //加载type指向的类并且创建实例
            ObjectFactory factory = (ObjectFactory) resolveClass(type).newInstance();
            //设置配置
            factory.setProperties(properties);
            //将对象工厂设置给 全局配置对象
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

    /**
     * 此方法的作用是用于解析 xml配置中 properties 元素的配置
     * 此元素配置的作用参考如下官方文档
     * http://www.mybatis.org/mybatis-3/zh/configuration.html#properties
     * 此配置的主要作用就是 通过将XML中的配置值抽取到专门的properties文件中,这个文件的位置可以再本地的一个资源
     * 或者是一个网络上的资源
     *
     * @param context
     * @throws Exception
     */
    private void propertiesElement(XNode context) throws Exception {
        if (context != null) {
            //解析传入元素中的 name和value 属性,并且设置到 properties 对象中.
            //properties 标签 内部也可已配置一些设置,具体参考上面文档链接中对此有解释
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
                //使用 resource 字段指定的是一个本地的 properties 资源
                defaults.putAll(Resources.getResourceAsProperties(resource));
            } else if (url != null) {
                //如果 配置了 url 则根据 url 加载配置,加载远程配置资源的.
                defaults.putAll(Resources.getUrlAsProperties(url));
            }
            Properties vars = configuration.getVariables();
            if (vars != null) {
                //将 configuration 配置中的数据 放入 defaults中,合并两部分的配置
                defaults.putAll(vars);
            }
            //更新 XpathParser 和 Configuration 中的配置数据
            //这里defaults的数据在解析 XML文档中的通过占位符(${xxx})配置的值
            parser.setVariables(defaults);
            //将配置设置到 全局配置对象(configuration)中去
            //TODO 这里我有个疑问,XML中的配置什么怎么处理,也就是 XML中配置了但是 properties文件中没有配置的属性
            configuration.setVariables(defaults);
        }
    }

    /**
     * 此方法将在配置文件中解析到的配置数据,设置到 全局配置对象(configuration)中.
     *
     * @param props
     * @throws Exception
     */
    private void settingsElement(Properties props) throws Exception {
        //TODO 此配置需要写代码验证
        //结果集映射配置, 默认配置为 PARTIAL.
        configuration.setAutoMappingBehavior(AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior", "PARTIAL")));

        //TODO 临时注释 涉及到 mapping 模块
        configuration.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.valueOf(props.getProperty("autoMappingUnknownColumnBehavior", "NONE")));

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

    /**
     * 从配置中读取类型转换器信息,完成类型转换器初始化.
     *
     * @param parent
     * @throws Exception
     */
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

    /**
     * 初始化配置的映射器
     * 参考官方文档:http://www.mybatis.org/mybatis-3/zh/configuration.html#mappers
     * MyBatis提供了四种配置映射器的方式
     * 第一 使用 resource 配置相对路径的XML
     * 第二 使用 url 配置指定路径下的XML
     * 第三 使用 class 指定一个接口
     * 第四 使用 package 指定一个包,会注册包下所有的接口
     * <p>
     * 特别注意:
     * MyBatis是支持mapper接口和XML两种方式配置SQL语句,并且允许这两种方式同时存在.
     * 因此上面四种配置方式从根本上说可以分为两大类, 一种是配置mapper接口,另一种 就是 配置XML
     * 从下面代码分析可以知道,无论是上面那种方式配置,都是 调用 MapperRegistry.addMapper这个方法
     * 这个方法中 解析注解配置,同时回去解析XML配置,解析XML时也会调用MapperRegistry.addMapper方法,通过判断是否加载避免死循环.
     *
     * @param parent
     * @throws Exception
     */
    private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                //如果配置文件中指定了 package 则获取包名
                if ("package".equals(child.getName())) {
                    String mapperPackage = child.getStringAttribute("name");
                    //TODO 注释 使用 package 指定一个包,注册包下所有的接口
                    configuration.addMappers(mapperPackage);
                } else {
                    String resource = child.getStringAttribute("resource");
                    String url = child.getStringAttribute("url");
                    String mapperClass = child.getStringAttribute("class");

                    // 判断是否是指定 resource 属性
                    if (resource != null && url == null && mapperClass == null) {
                        //TODO 暂时还不了解
                        ErrorContext.instance().resource(resource);

                        InputStream inputStream = Resources.getResourceAsStream(resource);
                        //加载本地相对路径XML文件 用于注册映射器
                        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
                        //parse 方法是解析xml的核心方法,最终目的就是讲解析结果添加到 configuration 中去.
                        //解析流程就在parse方法中.
                        mapperParser.parse();
                    } else if (resource == null && url != null && mapperClass == null) {
                        //TODO 暂时还不了解
                        ErrorContext.instance().resource(url);

                        InputStream inputStream = Resources.getUrlAsStream(url);
                        //加载资源限定符指定的XML文件 用于注册映射器
                        //和上面一个判断里面的作用是一样的只是换了一种资源加载的方式而已
                        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
                        mapperParser.parse();
                    } else if (resource == null && url == null && mapperClass != null) {
                        Class<?> mapperInterface = Resources.classForName(mapperClass);
                        //使用 class 指定一个接口,将此接口注册为一个映射器
                        configuration.addMapper(mapperInterface);
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
