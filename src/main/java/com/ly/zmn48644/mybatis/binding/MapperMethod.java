
package com.ly.zmn48644.mybatis.binding;


import com.ly.zmn48644.mybatis.session.Configuration;
import com.ly.zmn48644.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.util.HashMap;


public class MapperMethod {
    public <T> MapperMethod(Class<T> mapperInterface, Method method, Configuration configuration) {


    }

    public Object execute(SqlSession sqlSession, Object[] args) {
        return null;
    }
    //TODO 临时注释

    public static class ParamMap<V> extends HashMap<String, V> {
        private static final long serialVersionUID = -2212268410512043556L;
        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }

    }
}
