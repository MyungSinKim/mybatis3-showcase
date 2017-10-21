
package com.ly.zmn48644.mybatis.session;

import com.ly.zmn48644.mybatis.io.VFS;
import com.ly.zmn48644.mybatis.logging.Log;
import com.ly.zmn48644.mybatis.reflection.DefaultReflectorFactory;
import com.ly.zmn48644.mybatis.reflection.ReflectorFactory;
import com.ly.zmn48644.mybatis.reflection.factory.DefaultObjectFactory;
import com.ly.zmn48644.mybatis.reflection.factory.ObjectFactory;
import com.ly.zmn48644.mybatis.reflection.warpper.DefaultObjectWrapperFactory;
import com.ly.zmn48644.mybatis.reflection.warpper.ObjectWrapperFactory;
import com.ly.zmn48644.mybatis.type.JdbcType;
import com.ly.zmn48644.mybatis.type.TypeAliasRegistry;
import com.ly.zmn48644.mybatis.type.TypeHandlerRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * 全局配置对象
 */
public class Configuration {
    //TODO 未完成 Configuration


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
}
