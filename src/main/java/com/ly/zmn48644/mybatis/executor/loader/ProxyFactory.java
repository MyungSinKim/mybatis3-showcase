
package com.ly.zmn48644.mybatis.executor.loader;


import com.ly.zmn48644.mybatis.reflection.factory.ObjectFactory;
import com.ly.zmn48644.mybatis.session.Configuration;

import java.util.List;
import java.util.Properties;

public interface ProxyFactory {

    void setProperties(Properties properties);

    Object createProxy(Object target, ResultLoaderMap lazyLoader, Configuration configuration, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

}
