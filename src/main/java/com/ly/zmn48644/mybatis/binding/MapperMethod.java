
package com.ly.zmn48644.mybatis.binding;


import com.ly.zmn48644.mybatis.session.Configuration;
import com.ly.zmn48644.mybatis.session.SqlSession;

import java.lang.reflect.Method;


public class MapperMethod {
    public <T> MapperMethod(Class<T> mapperInterface, Method method, Configuration configuration) {


    }

    public Object execute(SqlSession sqlSession, Object[] args) {
        return null;
    }
    //TODO 临时注释
}
