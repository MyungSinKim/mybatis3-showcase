

package com.ly.zmn48644.mybatis.reflection;


import com.ly.zmn48644.mybatis.lang.UsesJava8;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

@UsesJava8
public class ParamNameUtil {
    public static List<String> getParamNames(Method method) {
        return getParameterNames(method);
    }

    public static List<String> getParamNames(Constructor<?> constructor) {
        return getParameterNames(constructor);
    }

    private static List<String> getParameterNames(Executable executable) {
        final List<String> names = new ArrayList<String>();
        final Parameter[] params = executable.getParameters();
        for (Parameter param : params) {
            names.add(param.getName());
        }
        return names;
    }

    private ParamNameUtil() {
        super();
    }
}
