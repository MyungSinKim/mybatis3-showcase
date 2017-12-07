package com.ly.zmn48644.mybatis.reflection;

import com.ly.zmn48644.mybatis.session.Configuration;

import java.lang.reflect.Method;

public class ParamNameResolverTest {

    public static void main(String[] args) throws NoSuchMethodException {
        Configuration configuration = new Configuration();

        Method method = ParamNameResolverTest.class.getMethod("tt", String.class, String.class);

        ParamNameResolver pnr = new ParamNameResolver(configuration,method);

        Object namedParams = pnr.getNamedParams(new Object[]{"zmn", "27"});

//        Method method = ParamNameResolverTest.class.getMethod("p", Integer.class);
//
//        ParamNameResolver pnr = new ParamNameResolver(configuration,method);
//
//        Object namedParams = pnr.getNamedParams(new Object[]{12});

        System.out.println(namedParams.toString());//{name=zmn, param1=zmn, age=27, param2=27}
        for (String s : pnr.getNames()) {
            System.out.println(s);
        }

    }

    public void p(Integer count){
        System.out.println(count);
    }
    public void tt(String name,String age){
        System.out.println(name+":"+age);
    }
}
