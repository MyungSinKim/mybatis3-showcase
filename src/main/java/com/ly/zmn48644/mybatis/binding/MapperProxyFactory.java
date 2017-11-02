
package com.ly.zmn48644.mybatis.binding;


import com.ly.zmn48644.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代理工厂,用于生成各种 mapper 的代理对象
 *
 * @param <T>
 */
public class MapperProxyFactory<T> {

    //mapper 接口类
    private final Class<T> mapperInterface;

    private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Class<T> getMapperInterface() {
        return mapperInterface;
    }

    public Map<Method, MapperMethod> getMethodCache() {
        return methodCache;
    }


    /**
     * 使用jdk中提供的Proxy类生成代理对象
     *
     * @param mapperProxy
     * @return
     */
    protected T newInstance(MapperProxy<T> mapperProxy) {
        //这里注意他使用的类加载器
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }

    /**
     * MapperRegistry 中调用此方法获取 代理对象
     *
     * @param sqlSession
     * @return
     */
    public T newInstance(SqlSession sqlSession) {
        //创建 MapperProxy 对象
        final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
        return newInstance(mapperProxy);
    }

}
