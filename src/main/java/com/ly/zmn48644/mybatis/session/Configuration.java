
package com.ly.zmn48644.mybatis.session;

import com.ly.zmn48644.mybatis.binding.MapperRegistry;
import com.ly.zmn48644.mybatis.io.VFS;
import com.ly.zmn48644.mybatis.logging.Log;
import com.ly.zmn48644.mybatis.mapping.Environment;
import com.ly.zmn48644.mybatis.parsing.XNode;
import com.ly.zmn48644.mybatis.reflection.DefaultReflectorFactory;
import com.ly.zmn48644.mybatis.reflection.MetaObject;
import com.ly.zmn48644.mybatis.reflection.ReflectorFactory;
import com.ly.zmn48644.mybatis.reflection.factory.DefaultObjectFactory;
import com.ly.zmn48644.mybatis.reflection.factory.ObjectFactory;
import com.ly.zmn48644.mybatis.reflection.warpper.DefaultObjectWrapperFactory;
import com.ly.zmn48644.mybatis.reflection.warpper.ObjectWrapperFactory;
import com.ly.zmn48644.mybatis.type.JdbcType;
import com.ly.zmn48644.mybatis.type.TypeAliasRegistry;
import com.ly.zmn48644.mybatis.type.TypeHandler;
import com.ly.zmn48644.mybatis.type.TypeHandlerRegistry;

import java.util.*;

/**
 * 全局配置对象
 */
public class Configuration {
    //TODO 未完成 Configuration


    protected final Set<String> loadedResources = new HashSet<String>();

    /**
     * 用于存储可复用的SQL片段
     */
    //TODO 这块还是有些疑问,暂时不深入.
    protected final Map<String, XNode> sqlFragments = new StrictMap<XNode>("XML fragments parsed from previous mappers");

    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }

    protected Environment environment;

    /**
     * Configuration factory class.
     * Used to create Configuration for loading deserialized unread properties.
     */
    protected Class<?> configurationFactory;

    protected boolean returnInstanceForEmptyRow;

    protected String logPrefix;

    //是否使用实际参数名,默认为false
    //mybatis传多个参数(不使用@param注解情况下),3.4.2版本之后使用#{0}-#{n}引起的参数绑定异常.
    //3.4.2版本之后settings属性中useActualParamName参数的默认值为true,参数才可以不使用@param.
    protected boolean useActualParamName = true;

    protected boolean callSettersOnNulls;
    //触发懒加载属性加载的方法
    protected Set<String> lazyLoadTriggerMethods = new HashSet<String>(Arrays.asList(new String[]{"equals", "clone", "hashCode", "toString"}));
    protected JdbcType jdbcTypeForNull = JdbcType.OTHER;

    protected LocalCacheScope localCacheScope = LocalCacheScope.SESSION;
    //是否允许在嵌套语句中使用分页
    protected boolean safeRowBoundsEnabled;

    protected boolean safeResultHandlerEnabled = true;

    //是否启用 下划线命名表端 到 驼峰命名属性映射
    protected boolean mapUnderscoreToCamelCase;

    protected Integer defaultStatementTimeout;
    protected Integer defaultFetchSize;
    protected ExecutorType defaultExecutorType = ExecutorType.SIMPLE;


    //全局配置 是否使用自动生成的主键 , 也可以在 insert 语句总专门指定.
    protected boolean useGeneratedKeys;
    //使用列标签代替列名
    protected boolean useColumnLabel = true;
    //是否允许单一语句返回多结果集
    protected boolean multipleResultSetsEnabled = true;
    //积极懒加载配置
    protected boolean aggressiveLazyLoading;
    //是否启用懒加载机制
    protected boolean lazyLoadingEnabled = false;

    //全局性的缓存配置,默认是启用缓存的.
    protected boolean cacheEnabled = true;

    //自动映射行为 的配置, 默认为 部分映射.
    protected AutoMappingBehavior autoMappingBehavior = AutoMappingBehavior.PARTIAL;

    //类型转换处理器
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
    //类型别名处理器
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    //保存全局性配置
    protected Properties variables = new Properties();

    //日志实现类
    protected Class<? extends Log> logImpl;
    //虚拟文件系统实现类
    protected Class<? extends VFS> vfsImpl;

    //反射器工厂
    protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();

    //对象工厂
    protected ObjectFactory objectFactory = new DefaultObjectFactory();

    //对象包装器工厂
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();


    //初始化 Mapper 注册中心
    protected final MapperRegistry mapperRegistry = new MapperRegistry(this);


    public Map<String, XNode> getSqlFragments() {
        return sqlFragments;
    }

    /**
     * 添加一个已经加载的资源到loadedResources
     * @param resource
     */
    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    /**
     * 判断资源是否已经加载
     * @param resource
     * @return
     */
    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }


    /**
     * 将一个指定包下的接口全部注册到映射器注册中心
     *
     * @param packageName
     */
    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    /**
     * 添加一个指定接口到注册器中心
     *
     * @param type
     * @param <T>
     */
    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    /**
     * @param type
     * @param sqlSession
     * @param <T>
     * @return
     */
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    /**
     * 设置默认枚举类型处理器
     *
     * @param typeHandler
     */
    public void setDefaultEnumTypeHandler(Class<? extends TypeHandler> typeHandler) {
        if (typeHandler != null) {
            getTypeHandlerRegistry().setDefaultEnumTypeHandler(typeHandler);
        }
    }

    public boolean isCallSettersOnNulls() {
        return callSettersOnNulls;
    }

    public void setCallSettersOnNulls(boolean callSettersOnNulls) {
        this.callSettersOnNulls = callSettersOnNulls;
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    public Properties getVariables() {
        return variables;
    }

    public Class<? extends Log> getLogImpl() {
        return logImpl;
    }

    public void setLogImpl(Class<? extends Log> logImpl) {
        this.logImpl = logImpl;
    }

    public Class<? extends VFS> getVfsImpl() {
        return vfsImpl;
    }

    public ReflectorFactory getReflectorFactory() {
        return reflectorFactory;
    }

    public void setReflectorFactory(ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }

    public void setObjectWrapperFactory(ObjectWrapperFactory objectWrapperFactory) {
        this.objectWrapperFactory = objectWrapperFactory;
    }

    public void setVariables(Properties variables) {
        this.variables = variables;
    }

    public AutoMappingBehavior getAutoMappingBehavior() {
        return autoMappingBehavior;
    }

    public void setAutoMappingBehavior(AutoMappingBehavior autoMappingBehavior) {
        this.autoMappingBehavior = autoMappingBehavior;
    }

    public void setVfsImpl(Class<? extends VFS> vfsImpl) {
        if (vfsImpl != null) {
            this.vfsImpl = vfsImpl;
            VFS.addImplClass(this.vfsImpl);
        }
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public boolean isLazyLoadingEnabled() {
        return lazyLoadingEnabled;
    }

    public void setLazyLoadingEnabled(boolean lazyLoadingEnabled) {
        this.lazyLoadingEnabled = lazyLoadingEnabled;
    }

    public boolean isAggressiveLazyLoading() {
        return aggressiveLazyLoading;
    }

    public void setAggressiveLazyLoading(boolean aggressiveLazyLoading) {
        this.aggressiveLazyLoading = aggressiveLazyLoading;
    }

    public boolean isMultipleResultSetsEnabled() {
        return multipleResultSetsEnabled;
    }

    public void setMultipleResultSetsEnabled(boolean multipleResultSetsEnabled) {
        this.multipleResultSetsEnabled = multipleResultSetsEnabled;
    }

    public boolean isUseColumnLabel() {
        return useColumnLabel;
    }

    public void setUseColumnLabel(boolean useColumnLabel) {
        this.useColumnLabel = useColumnLabel;
    }

    public boolean isUseGeneratedKeys() {
        return useGeneratedKeys;
    }

    public void setUseGeneratedKeys(boolean useGeneratedKeys) {
        this.useGeneratedKeys = useGeneratedKeys;
    }

    public Integer getDefaultStatementTimeout() {
        return defaultStatementTimeout;
    }

    public void setDefaultStatementTimeout(Integer defaultStatementTimeout) {
        this.defaultStatementTimeout = defaultStatementTimeout;
    }

    public Integer getDefaultFetchSize() {
        return defaultFetchSize;
    }

    public void setDefaultFetchSize(Integer defaultFetchSize) {
        this.defaultFetchSize = defaultFetchSize;
    }

    public ExecutorType getDefaultExecutorType() {
        return defaultExecutorType;
    }

    public void setDefaultExecutorType(ExecutorType defaultExecutorType) {
        this.defaultExecutorType = defaultExecutorType;
    }

    public boolean isMapUnderscoreToCamelCase() {
        return mapUnderscoreToCamelCase;
    }

    public void setMapUnderscoreToCamelCase(boolean mapUnderscoreToCamelCase) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    public boolean isSafeRowBoundsEnabled() {
        return safeRowBoundsEnabled;
    }

    public void setSafeRowBoundsEnabled(boolean safeRowBoundsEnabled) {
        this.safeRowBoundsEnabled = safeRowBoundsEnabled;
    }

    public boolean isSafeResultHandlerEnabled() {
        return safeResultHandlerEnabled;
    }

    public void setSafeResultHandlerEnabled(boolean safeResultHandlerEnabled) {
        this.safeResultHandlerEnabled = safeResultHandlerEnabled;
    }

    public LocalCacheScope getLocalCacheScope() {
        return localCacheScope;
    }

    public void setLocalCacheScope(LocalCacheScope localCacheScope) {
        this.localCacheScope = localCacheScope;
    }

    public JdbcType getJdbcTypeForNull() {
        return jdbcTypeForNull;
    }

    public void setJdbcTypeForNull(JdbcType jdbcTypeForNull) {
        this.jdbcTypeForNull = jdbcTypeForNull;
    }

    public Set<String> getLazyLoadTriggerMethods() {
        return lazyLoadTriggerMethods;
    }

    public void setLazyLoadTriggerMethods(Set<String> lazyLoadTriggerMethods) {
        this.lazyLoadTriggerMethods = lazyLoadTriggerMethods;
    }

    public boolean isUseActualParamName() {
        return useActualParamName;
    }

    public void setUseActualParamName(boolean useActualParamName) {
        this.useActualParamName = useActualParamName;
    }

    public boolean isReturnInstanceForEmptyRow() {
        return returnInstanceForEmptyRow;
    }

    public void setReturnInstanceForEmptyRow(boolean returnInstanceForEmptyRow) {
        this.returnInstanceForEmptyRow = returnInstanceForEmptyRow;
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    public void setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
    }

    public Class<?> getConfigurationFactory() {
        return configurationFactory;
    }

    public void setConfigurationFactory(Class<?> configurationFactory) {
        this.configurationFactory = configurationFactory;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    /**
     * 静态内部类
     *
     * @param <V>
     */
    protected static class StrictMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -4950446264854982944L;
        private final String name;

        public StrictMap(String name, int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
            this.name = name;
        }

        public StrictMap(String name, int initialCapacity) {
            super(initialCapacity);
            this.name = name;
        }

        public StrictMap(String name) {
            super();
            this.name = name;
        }

        public StrictMap(String name, Map<String, ? extends V> m) {
            super(m);
            this.name = name;
        }

        @SuppressWarnings("unchecked")
        public V put(String key, V value) {
            if (containsKey(key)) {
                throw new IllegalArgumentException(name + " already contains value for " + key);
            }
            if (key.contains(".")) {
                final String shortKey = getShortName(key);
                if (super.get(shortKey) == null) {
                    super.put(shortKey, value);
                } else {
                    super.put(shortKey, (V) new Ambiguity(shortKey));
                }
            }
            return super.put(key, value);
        }

        public V get(Object key) {
            V value = super.get(key);
            if (value == null) {
                throw new IllegalArgumentException(name + " does not contain value for " + key);
            }
            if (value instanceof Ambiguity) {
                throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
                        + " (try using the full name including the namespace, or rename one of the entries)");
            }
            return value;
        }

        private String getShortName(String key) {
            final String[] keyParts = key.split("\\.");
            return keyParts[keyParts.length - 1];
        }

        protected static class Ambiguity {
            final private String subject;

            public Ambiguity(String subject) {
                this.subject = subject;
            }

            public String getSubject() {
                return subject;
            }
        }
    }
}
