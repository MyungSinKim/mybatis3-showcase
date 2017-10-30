

package com.ly.zmn48644.mybatis.reflection;


import com.ly.zmn48644.mybatis.annotations.Param;
import com.ly.zmn48644.mybatis.session.Configuration;
import com.ly.zmn48644.mybatis.session.ResultHandler;
import com.ly.zmn48644.mybatis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ParamNameResolver {

    private static final String GENERIC_NAME_PREFIX = "param";


    private final SortedMap<Integer, String> names;

    private boolean hasParamAnnotation;

    public ParamNameResolver(Configuration config, Method method) {
        final Class<?>[] paramTypes = method.getParameterTypes();
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        final SortedMap<Integer, String> map = new TreeMap<Integer, String>();
        int paramCount = paramAnnotations.length;
        // get names from @Param annotations
        for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
            if (isSpecialParameter(paramTypes[paramIndex])) {
                // skip special parameters
                continue;
            }
            String name = null;
            for (Annotation annotation : paramAnnotations[paramIndex]) {
                if (annotation instanceof Param) {
                    hasParamAnnotation = true;
                    name = ((Param) annotation).value();
                    break;
                }
            }
            if (name == null) {
                // @Param was not specified.
                if (config.isUseActualParamName()) {
                    name = getActualParamName(method, paramIndex);
                }
                if (name == null) {
                    // use the parameter index as the name ("0", "1", ...)
                    // gcode issue #71
                    name = String.valueOf(map.size());
                }
            }
            map.put(paramIndex, name);
        }
        names = Collections.unmodifiableSortedMap(map);
    }

    private String getActualParamName(Method method, int paramIndex) {
        if (Jdk.parameterExists) {
            return ParamNameUtil.getParamNames(method).get(paramIndex);
        }
        return null;
    }

    private static boolean isSpecialParameter(Class<?> clazz) {
        return RowBounds.class.isAssignableFrom(clazz) || ResultHandler.class.isAssignableFrom(clazz);
    }

    /**
     * Returns parameter names referenced by SQL providers.
     */
    public String[] getNames() {
        return names.values().toArray(new String[0]);
    }

    /**
     * <p>
     * A single non-special parameter is returned without a name.<br />
     * Multiple parameters are named using the naming rule.<br />
     * In addition to the default names, this method also adds the generic names (param1, param2,
     * ...).
     * </p>
     */
    //TODO 临时注释
//  public Object getNamedParams(Object[] args) {
//    final int paramCount = names.size();
//    if (args == null || paramCount == 0) {
//      return null;
//    } else if (!hasParamAnnotation && paramCount == 1) {
//      return args[names.firstKey()];
//    } else {
//      final Map<String, Object> param = new MapperMethod.ParamMap<Object>();
//      int i = 0;
//      for (Map.Entry<Integer, String> entry : names.entrySet()) {
//        param.put(entry.getValue(), args[entry.getKey()]);
//        // add generic param names (param1, param2, ...)
//        final String genericParamName = GENERIC_NAME_PREFIX + String.valueOf(i + 1);
//        // ensure not to overwrite parameter named with @Param
//        if (!names.containsValue(genericParamName)) {
//          param.put(genericParamName, args[entry.getKey()]);
//        }
//        i++;
//      }
//      return param;
//    }
//  }
}
