
package com.ly.zmn48644.mybatis.executor.resultset;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveTypes {
    private final Map<Class<?>, Class<?>> primitiveToWrappers;
    private final Map<Class<?>, Class<?>> wrappersToPrimitives;

    public PrimitiveTypes() {
        this.primitiveToWrappers = new HashMap<Class<?>, Class<?>>();
        this.wrappersToPrimitives = new HashMap<Class<?>, Class<?>>();

        add(boolean.class, Boolean.class);
        add(byte.class, Byte.class);
        add(char.class, Character.class);
        add(double.class, Double.class);
        add(float.class, Float.class);
        add(int.class, Integer.class);
        add(long.class, Long.class);
        add(short.class, Short.class);
        add(void.class, Void.class);
    }

    private void add(final Class<?> primitiveType, final Class<?> wrapperType) {
        primitiveToWrappers.put(primitiveType, wrapperType);
        wrappersToPrimitives.put(wrapperType, primitiveType);
    }

    public Class<?> getWrapper(final Class<?> primitiveType) {
        return primitiveToWrappers.get(primitiveType);
    }

    public Class<?> getPrimitive(final Class<?> wrapperType) {
        return wrappersToPrimitives.get(wrapperType);
    }
}
