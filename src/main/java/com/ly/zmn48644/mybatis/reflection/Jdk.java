
package com.ly.zmn48644.mybatis.reflection;


import com.ly.zmn48644.mybatis.io.Resources;

/**
 * To check the existence of version dependent classes.
 */
public class Jdk {

    /**
     * <code>true</code> if <code>java.lang.reflect.Parameter</code> is available.
     */
    public static final boolean parameterExists;

    static {
        boolean available = false;
        try {
            Resources.classForName("java.lang.reflect.Parameter");
            available = true;
        } catch (ClassNotFoundException e) {
            // ignore
        }
        parameterExists = available;
    }

    public static final boolean dateAndTimeApiExists;

    static {
        boolean available = false;
        try {
            Resources.classForName("java.time.Clock");
            available = true;
        } catch (ClassNotFoundException e) {
            // ignore
        }
        dateAndTimeApiExists = available;
    }

    private Jdk() {
        super();
    }
}
