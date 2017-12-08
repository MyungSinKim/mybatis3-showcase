
package com.ly.zmn48644.mybatis.binding;


import com.ly.zmn48644.mybatis.lang.UsesJava7;
import com.ly.zmn48644.mybatis.reflection.ExceptionUtil;
import com.ly.zmn48644.mybatis.session.SqlSession;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * 封装 mapper 代理逻辑的类
 *
 * @param <T>
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private static final long serialVersionUID = -6424540398559729838L;

    private final SqlSession sqlSession;
    //代理接口类型
    private final Class<T> mapperInterface;

    //这里缓存 MapperMethod 对象, 由于MapperMethod 在框架初始化的时候创建,在运行过程中是不会发生改变的
    //此处的methodCache 指向  MapperProxyFactory 的成员属性 methodCache.
    private final Map<Method, MapperMethod> methodCache;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    /**
     * 调用 mapper接口 中的方法 真正执行的就是此方法
     *
     * @param proxy  代理的对象
     * @param method 调用的方法
     * @param args   调用方法的参数
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        try {
            //method.getDeclaringClass()返回的是 method 所定义的类
            //这里判断 如果调用的方法是定义在 Object 类中
            if (Object.class.equals(method.getDeclaringClass())) {
                //调用 this 的 method 定义的方法.
                return method.invoke(this, args);
            } else if (isDefaultMethod(method)) {
                return invokeDefaultMethod(proxy, method, args);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
        //尝试从缓存中获取 MapperMethod 对象
        final MapperMethod mapperMethod = cachedMapperMethod(method);
        //调用 execute 方法
        return mapperMethod.execute(sqlSession, args);
    }

    private MapperMethod cachedMapperMethod(Method method) {
        //从缓存中获取
        MapperMethod mapperMethod = methodCache.get(method);
        //没有获取到
        if (mapperMethod == null) {
            //创建
            mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
            //放入缓存, methodCache 是 MapperProxyFactory 的一个成员属性!
            methodCache.put(method, mapperMethod);
        }
        return mapperMethod;
    }


    /**
     * 调用接口中定义的 接口 默认方法
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @UsesJava7
    private Object invokeDefaultMethod(Object proxy, Method method, Object[] args)
            throws Throwable {
        final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                .getDeclaredConstructor(Class.class, int.class);
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        final Class<?> declaringClass = method.getDeclaringClass();
        return constructor
                .newInstance(declaringClass,
                        MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                                | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC)
                .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
    }

    /**
     * Backport of java.lang.reflect.Method#isDefault()
     * java.lang.reflect.Method#isDefault() 的补丁
     * <p>
     * jdk 1.8 提供了接口的默认方法功能, 这里就是判断是否是接口中定义的默认方法
     */
    private boolean isDefaultMethod(Method method) {
        return ((method.getModifiers()
                & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC)
                && method.getDeclaringClass().isInterface();
    }
}
