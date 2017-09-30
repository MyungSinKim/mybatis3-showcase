
package com.ly.zmn48644.mybatis.reflection;


import com.ly.zmn48644.mybatis.domain.misc.RichType;
import com.ly.zmn48644.mybatis.domain.misc.generics.GenericConcrete;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MetaClassTest {

    private RichType rich = new RichType();

    //注意这种,初始的方法.
    Map<String, RichType> map = new HashMap<String, RichType>() {
        {
            //这里初始化方式没见过.
            put("richType", rich);
        }
    };


    public MetaClassTest() {
        rich.setRichType(new RichType());
    }


    /**
     * 测试获取普通方法的get/set方法的返回值类型,以及参数类型
     */
    @Test
    public void shouldTestDataTypeOfGenericMethod() {
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        //MetaClass 是对reflector的封装,提供了更加高级的功能比如,支持属性表达式.
        MetaClass meta = MetaClass.forClass(GenericConcrete.class, reflectorFactory);
        //判断id属性的get方法类型是否是long
        assertEquals(Long.class, meta.getGetterType("id"));
        //判断id属性的set方法是否是long
        assertEquals(Long.class, meta.getSetterType("id"));
    }

    /**
     * 去检查,某个属性是否存在get/set方法
     */
    @Test
    public void shouldCheckGetterExistance() {
        //通过反射类工厂获取,反射类对象
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);
        //判断 richField是否存在 get 方法. 预期是存在的.
        assertTrue(meta.hasGetter("richField"));
        assertTrue(meta.hasGetter("richProperty"));
        assertTrue(meta.hasGetter("richList"));
        assertTrue(meta.hasGetter("richMap"));

        //这里就体现出来, MetaClass 为什么是 Reflector 更高层次的封装了.
        //在 MetaClass 中实现了 下面这种 嵌套情况下,属性表达式的功能.
        assertTrue(meta.hasGetter("richList[0]"));

        assertTrue(meta.hasGetter("richType"));
        //嵌套对象属性,情况下的 判断. 这里使用了递归的思想
        assertTrue(meta.hasGetter("richType.richField"));
        assertTrue(meta.hasGetter("richType.richProperty"));
        assertTrue(meta.hasGetter("richType.richList"));
        assertTrue(meta.hasGetter("richType.richMap"));
        assertTrue(meta.hasGetter("richType.richList[0]"));

        assertEquals("richType.richProperty", meta.findProperty("richType.richProperty", false));

        assertFalse(meta.hasGetter("[0]"));
    }

    /**
     * 和上面一个测试方法类似, 这是测试 get方法的.不再多说.
     */
    @Test
    public void shouldCheckSetterExistance() {
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);
        assertTrue(meta.hasSetter("richField"));
        assertTrue(meta.hasSetter("richProperty"));
        assertTrue(meta.hasSetter("richList"));
        assertTrue(meta.hasSetter("richMap"));
        assertTrue(meta.hasSetter("richList[0]"));

        assertTrue(meta.hasSetter("richType"));
        assertTrue(meta.hasSetter("richType.richField"));
        assertTrue(meta.hasSetter("richType.richProperty"));
        assertTrue(meta.hasSetter("richType.richList"));
        assertTrue(meta.hasSetter("richType.richMap"));
        assertTrue(meta.hasSetter("richType.richList[0]"));

        assertFalse(meta.hasSetter("[0]"));
    }


    /**
     * 检查测试对象所有的get方法返回值是否正确.
     */
    @Test
    public void shouldCheckTypeForEachGetter() {
        //创建反射工厂, 这里突然想到, 在MyBatis中 是 谁创建的 , 在什么地方创建的 , 起了什么作用.
        //这里想到昨天在 ss.gg 上看到的 设计模式的三个层次, 其中说到 谁(创建) 在什么地方(结构) 做了什么(行为).
        //使用上面的思路去分析整个复杂系统,是一个很好的思路.
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();

        MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);
        //判断这个类的 richField 属性的 get方法返回值 是否是String类型
        assertEquals(String.class, meta.getGetterType("richField"));
        assertEquals(String.class, meta.getGetterType("richProperty"));
        assertEquals(List.class, meta.getGetterType("richList"));
        assertEquals(Map.class, meta.getGetterType("richMap"));
        //集合属性的情况下获取get方法返回值.
        assertEquals(List.class, meta.getGetterType("richList[0]"));
        assertEquals(RichType.class, meta.getGetterType("richType"));
        assertEquals(String.class, meta.getGetterType("richType.richField"));
        assertEquals(String.class, meta.getGetterType("richType.richProperty"));
        assertEquals(List.class, meta.getGetterType("richType.richList"));
        assertEquals(Map.class, meta.getGetterType("richType.richMap"));
        assertEquals(List.class, meta.getGetterType("richType.richList[0]"));
    }

    /**
     * 和上面的测试方法相同,测试 set方法的参数类型.
     */
    @Test
    public void shouldCheckTypeForEachSetter() {

        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();

        MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);

        assertEquals(String.class, meta.getSetterType("richField"));
        assertEquals(String.class, meta.getSetterType("richProperty"));
        assertEquals(List.class, meta.getSetterType("richList"));
        assertEquals(Map.class, meta.getSetterType("richMap"));
        assertEquals(List.class, meta.getSetterType("richList[0]"));

        assertEquals(RichType.class, meta.getSetterType("richType"));
        assertEquals(String.class, meta.getSetterType("richType.richField"));
        assertEquals(String.class, meta.getSetterType("richType.richProperty"));
        assertEquals(List.class, meta.getSetterType("richType.richList"));
        assertEquals(Map.class, meta.getSetterType("richType.richMap"));
        assertEquals(List.class, meta.getSetterType("richType.richList[0]"));
    }

    /**
     * 获取类中,所有get方法名,返回的是一个数组.
     */
    @Test
    public void shouldCheckGetterAndSetterNames() {
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);
        //这里我突然想到, 优先使用数组,性能会好很多.
        assertEquals(5, meta.getGetterNames().length);
        assertEquals(5, meta.getSetterNames().length);
    }

    /**
     * 忽略大小写,的方式根据属性表达式查找属性.
     */
    @Test
    public void shouldFindPropertyName() {
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);
        assertEquals("richType.richType", meta.findProperty("richType.richTypE"));
    }

}
