
package com.ly.zmn48644.mybatis.binding;

import com.ly.zmn48644.mybatis.builder.annotation.MapperAnnotationBuilder;
import com.ly.zmn48644.mybatis.io.ResolverUtil;
import com.ly.zmn48644.mybatis.session.Configuration;
import com.ly.zmn48644.mybatis.session.SqlSession;

import java.util.*;

/**
 * mapper 注册中心
 */
public class MapperRegistry {

    //全局配置对象
    private final Configuration config;

    //保存 mapper 接口 和 mapper代理对象工厂的对应关系.
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<Class<?>, MapperProxyFactory<?>>();

    /**
     * 构造方法 , 传入全局配置对象 .
     *
     * @param config
     */
    public MapperRegistry(Configuration config) {
        this.config = config;
    }


    /**
     * 从代理工厂生成 mapper 代理对象
     *
     * @param type
     * @param sqlSession
     * @param <T>
     * @return
     */
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        //获取代理对象工厂
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            //从代理工厂中创建 mapper 代理对象.
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }


    /**
     * 判断是否已经注册
     *
     * @param type
     * @param <T>
     * @return
     */
    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    /**
     * 向映射器注册中心添加新的映射器
     * 向knownMappers中添加一个键值对,key是接口类型就是type,value是MapperProxyFactory(映射器代理对象工厂)
     *
     * @param type
     * @param <T>
     */
    public <T> void addMapper(Class<T> type) {
        //type必须是一个接口类型
        if (type.isInterface()) {
            //判断是否是重复的注册
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            //添加完成标记,初始化为false
            boolean loadCompleted = false;
            try {
                knownMappers.put(type, new MapperProxyFactory<T>(type));
                // It's important that the type is added before the parser is run
                // otherwise the binding may automatically be attempted by the
                // mapper parser. If the type is already known, it won't try.

                //解析接口中通过注解配置的SQL
                MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
                parser.parse();
                //添加完成设置为true
                loadCompleted = true;
            } finally {
                //如果上面的执行过程中报错,loadCompleted就为false
                if (!loadCompleted) {
                    //移除knownMappers中添加的
                    knownMappers.remove(type);
                }
            }
        }
    }

    /**
     * @since 3.2.2
     */
    public Collection<Class<?>> getMappers() {
        return Collections.unmodifiableCollection(knownMappers.keySet());
    }

    /**
     * @since 3.2.2
     */
    public void addMappers(String packageName, Class<?> superType) {
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<Class<?>>();
        resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
        Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();
        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }

    /**
     * @since 3.2.2
     */
    public void addMappers(String packageName) {
        addMappers(packageName, Object.class);
    }

}
