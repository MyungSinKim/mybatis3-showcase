

package com.ly.zmn48644.mybatis.reflection;


import com.ly.zmn48644.mybatis.annotations.Param;
import com.ly.zmn48644.mybatis.binding.MapperMethod;
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


    /**
     * key 为 参数所处的位置 0 ,1 ,2 等
     * value 为 参数的名字
     */
    private final SortedMap<Integer, String> names;

    private boolean hasParamAnnotation;

    public ParamNameResolver(Configuration config, Method method) {
        //通过反射获取到方法参数类型列表
        final Class<?>[] paramTypes = method.getParameterTypes();
        //通过反射获取到方法参数上的注解,返回的是个二维数组
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        final SortedMap<Integer, String> map = new TreeMap<Integer, String>();

        int paramCount = paramAnnotations.length;
        //从@Param注解中获取参数名
        for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
            //判断是否是  RowBound 或者 ResultHandler 接口的实现类
            if (isSpecialParameter(paramTypes[paramIndex])) {
                //跳过指定的方法参数
                continue;
            }
            String name = null;
            //遍历所有方法参数上的注解,如果参数上存在注解就会进入到此循环
            for (Annotation annotation : paramAnnotations[paramIndex]) {
                //判断注解的类型是否是
                if (annotation instanceof Param) {
                    //将 是否存在 param 注解标记 设置为 true
                    hasParamAnnotation = true;
                    //获取注解中 设置的参数名
                    name = ((Param) annotation).value();
                    //跳出循环
                    break;
                }
            }
            if (name == null) {
                //进入到此说明 参数上没有通过注解设置 参数名称
                // @Param was not specified.
                //判断全局配置
                if (config.isUseActualParamName()) {
                    name = getActualParamName(method, paramIndex);
                }
                //如果不能获取到name
                if (name == null) {
                    //使用 参数索引位 作为参数名字
                    name = String.valueOf(map.size());
                }
            }
            map.put(paramIndex, name);
        }
        //装饰为不可变集合
        names = Collections.unmodifiableSortedMap(map);
    }

    /**
     * @param method
     * @param paramIndex
     * @return
     */
    private String getActualParamName(Method method, int paramIndex) {
        if (Jdk.parameterExists) {
            //进入到此说明使用的JDK版本是1.8
            //根据参数索引 paramIndex 参数获取参数名
            return ParamNameUtil.getParamNames(method).get(paramIndex);
        }
        return null;
    }

    //判断参数的类型是否是 指定的 RowBounds 或者 ResultHandler 接口的实现
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
    public Object getNamedParams(Object[] args) {
        //获取参数的总数量
        final int paramCount = names.size();
        //如果 args 为空 或者 names 为空
        if (args == null || paramCount == 0) {
            //返回空 , 说明 接口中的方法 无参数.
            return null;
        } else if (!hasParamAnnotation && paramCount == 1) {
            //如果方法只有 一个参数并且没有使用注解重新定义参数名,直接返回此参数的值
            return args[names.firstKey()];
        } else {
            //进入到此 说明 参数中使用了 注解,或者 方法参数有多个
            //创建一个用于存储 参数名 和 参数值对应关系的map.
            final Map<String, Object> param = new MapperMethod.ParamMap<Object>();
            int i = 0;
            for (Map.Entry<Integer, String> entry : names.entrySet()) {
                //获取到参数名和参数值,放到 param 中.
                param.put(entry.getValue(), args[entry.getKey()]);
                // 生成参数名为 param1 param2 ...的通用参数名
                final String genericParamName = GENERIC_NAME_PREFIX + String.valueOf(i + 1);
                // ensure not to overwrite parameter named with @Param
                //确保不覆盖 @Param定义的参数名
                if (!names.containsValue(genericParamName)) {
                    //将使用通用参数名的映射关系添加到 param 中.
                    param.put(genericParamName, args[entry.getKey()]);
                }
                i++;
            }
            return param;
        }
    }
}