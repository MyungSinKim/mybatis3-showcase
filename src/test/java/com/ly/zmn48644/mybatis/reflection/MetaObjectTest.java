
package com.ly.zmn48644.mybatis.reflection;

import com.ly.zmn48644.mybatis.domain.CustomBeanWrapper;
import com.ly.zmn48644.mybatis.domain.CustomBeanWrapperFactory;
import com.ly.zmn48644.mybatis.domain.RichType;
import com.ly.zmn48644.mybatis.domain.blog.Author;
import com.ly.zmn48644.mybatis.domain.blog.Section;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class MetaObjectTest {

    @Test
    public void shouldGetAndSetField() {
        RichType rich = new RichType();

        MetaObject meta = SystemMetaObject.forObject(rich);
        //通过 MetaObject 对象操作 设置/获取 属性值
        //注意这里使用的是 Field  也就是 richField 没有 set/get 方法
        meta.setValue("richField", "foo");
        assertEquals("foo", meta.getValue("richField"));
    }

    @Test
    public void shouldGetAndSetNestedField() {
        RichType rich = new RichType();
        MetaObject meta = SystemMetaObject.forObject(rich);
        //通过 MetaObject 对象操作 设置/获取 属性值 , 这里使用了复杂的属性嵌套.
        meta.setValue("richType.richField", "foo");
        assertEquals("foo", meta.getValue("richType.richField"));
    }

    @Test
    public void shouldGetAndSetProperty() {
        RichType rich = new RichType();
        MetaObject meta = SystemMetaObject.forObject(rich);
        //通过 MetaObject 对象操作 设置/获取 属性值.
        //Property 也就是 有 set/get 方法.
        meta.setValue("richProperty", "foo");
        assertEquals("foo", meta.getValue("richProperty"));
    }

    @Test
    public void shouldGetAndSetNestedProperty() {
        RichType rich = new RichType();
        MetaObject meta = SystemMetaObject.forObject(rich);
        //通过 MetaObject 对象操作 设置/获取 属性值 , 这里使用了复杂的属性嵌套.
        ////Property 也就是 有 set/get 方法.
        meta.setValue("richType.richProperty", "foo");
        assertEquals("foo", meta.getValue("richType.richProperty"));
    }

    @Test
    public void shouldGetAndSetMapPair() {
        RichType rich = new RichType();
        MetaObject meta = SystemMetaObject.forObject(rich);
        //通过 MetaObject 操作 属性类型为 map 的 属性, 这里是使用的.语法操作.
        meta.setValue("richMap.key", "foo");
        assertEquals("foo", meta.getValue("richMap.key"));
    }

    @Test
    public void shouldGetAndSetMapPairUsingArraySyntax() {
        RichType rich = new RichType();
        MetaObject meta = SystemMetaObject.forObject(rich);
        //通过 MetaObject 操作 属性类型为 map 的 属性, 这里是使用的 数组语法 操作 map.
        meta.setValue("richMap[key]", "foo");
        assertEquals("foo", meta.getValue("richMap[key]"));
    }

    @Test
    public void shouldGetAndSetNestedMapPair() {
        RichType rich = new RichType();
        MetaObject meta = SystemMetaObject.forObject(rich);
        //通过 MetaObject 操作 嵌套的 map 属性对象, 这里使用的是 .语法
        meta.setValue("richType.richMap.key", "foo");
        assertEquals("foo", meta.getValue("richType.richMap.key"));
    }

    @Test
    public void shouldGetAndSetNestedMapPairUsingArraySyntax() {
        RichType rich = new RichType();
        MetaObject meta = SystemMetaObject.forObject(rich);
        //嵌套的属性对象设置和获取, 这里使用了 数组语法.
        meta.setValue("richType.richMap[key]", "foo");
        assertEquals("foo", meta.getValue("richType.richMap[key]"));
    }

    @Test
    public void shouldGetAndSetListItem() {
        RichType rich = new RichType();
        MetaObject meta = SystemMetaObject.forObject(rich);
        //通过 MetaObject 对象操作 集合类型的属性,使用 数组语法.
        meta.setValue("richList[0]", "foo");
        assertEquals("foo", meta.getValue("richList[0]"));
    }


    /**
     * 这个和上面的一个 test 没看出来有啥区别啊
     */
    @Test
    public void shouldSetAndGetSelfListItem() {
        RichType rich = new RichType();
        MetaObject meta = SystemMetaObject.forObject(rich);
        meta.setValue("richList[0]", "foo");
        assertEquals("foo", meta.getValue("richList[0]"));
    }

    @Test
    public void shouldGetAndSetNestedListItem() {
        RichType rich = new RichType();
        MetaObject meta = SystemMetaObject.forObject(rich);
        //通过 MetaObject 对象操作 嵌套属性的集合,使用数组语法.
        meta.setValue("richType.richList[0]", "foo");
        assertEquals("foo", meta.getValue("richType.richList[0]"));
    }

    @Test
    public void shouldGetReadablePropertyNames() {
        RichType rich = new RichType();
        MetaObject meta = SystemMetaObject.forObject(rich);
        //获取所有的可读属性列表
        String[] readables = meta.getGetterNames();
        assertEquals(5, readables.length);
        for (String readable : readables) {
            assertTrue(meta.hasGetter(readable));
            assertTrue(meta.hasGetter("richType." + readable));
        }
        assertTrue(meta.hasGetter("richType"));
    }

    @Test
    public void shouldGetWriteablePropertyNames() {
        RichType rich = new RichType();
        MetaObject meta = SystemMetaObject.forObject(rich);
        //获取所有可写属性列表
        String[] writeables = meta.getSetterNames();
        assertEquals(5, writeables.length);
        for (String writeable : writeables) {
            //判断是否存在指定get 方法
            assertTrue(meta.hasSetter(writeable));
            //判断是否存在指定 set 方法
            assertTrue(meta.hasSetter("richType." + writeable));
        }
        assertTrue(meta.hasSetter("richType"));
    }

    @Test
    public void shouldSetPropertyOfNullNestedProperty() {
        MetaObject richWithNull = SystemMetaObject.forObject(new RichType());
        richWithNull.setValue("richType.richProperty", "foo");
        assertEquals("foo", richWithNull.getValue("richType.richProperty"));
    }

    @Test
    public void shouldSetPropertyOfNullNestedPropertyWithNull() {
        MetaObject richWithNull = SystemMetaObject.forObject(new RichType());
        richWithNull.setValue("richType.richProperty", null);
        assertEquals(null, richWithNull.getValue("richType.richProperty"));
    }

    @Test
    public void shouldGetPropertyOfNullNestedProperty() {
        MetaObject richWithNull = SystemMetaObject.forObject(new RichType());
        assertNull(richWithNull.getValue("richType.richProperty"));
    }

    @Test
    public void shouldVerifyHasReadablePropertiesReturnedByGetReadablePropertyNames() {
        MetaObject object = SystemMetaObject.forObject(new Author());
        for (String readable : object.getGetterNames()) {
            assertTrue(object.hasGetter(readable));
        }
    }

    @Test
    public void shouldVerifyHasWriteablePropertiesReturnedByGetWriteablePropertyNames() {
        MetaObject object = SystemMetaObject.forObject(new Author());
        for (String writeable : object.getSetterNames()) {
            assertTrue(object.hasSetter(writeable));
        }
    }

    @Test
    public void shouldSetAndGetProperties() {
        MetaObject object = SystemMetaObject.forObject(new Author());
        object.setValue("email", "test");
        assertEquals("test", object.getValue("email"));

    }

    @Test
    public void shouldVerifyPropertyTypes() {
        MetaObject object = SystemMetaObject.forObject(new Author());
        assertEquals(6, object.getSetterNames().length);
        assertEquals(int.class, object.getGetterType("id"));
        assertEquals(String.class, object.getGetterType("username"));
        assertEquals(String.class, object.getGetterType("password"));
        assertEquals(String.class, object.getGetterType("email"));
        assertEquals(String.class, object.getGetterType("bio"));
        assertEquals(Section.class, object.getGetterType("favouriteSection"));
    }

    @Test
    public void shouldDemonstrateDeeplyNestedMapProperties() {
        HashMap<String, String> map = new HashMap<String, String>();
        MetaObject metaMap = SystemMetaObject.forObject(map);

        assertTrue(metaMap.hasSetter("id"));
        assertTrue(metaMap.hasSetter("name.first"));
        assertTrue(metaMap.hasSetter("address.street"));

        assertFalse(metaMap.hasGetter("id"));
        assertFalse(metaMap.hasGetter("name.first"));
        assertFalse(metaMap.hasGetter("address.street"));

        metaMap.setValue("id", "100");
        metaMap.setValue("name.first", "Clinton");
        metaMap.setValue("name.last", "Begin");
        metaMap.setValue("address.street", "1 Some Street");
        metaMap.setValue("address.city", "This City");
        metaMap.setValue("address.province", "A Province");
        metaMap.setValue("address.postal_code", "1A3 4B6");

        assertTrue(metaMap.hasGetter("id"));
        assertTrue(metaMap.hasGetter("name.first"));
        assertTrue(metaMap.hasGetter("address.street"));

        assertEquals(3, metaMap.getGetterNames().length);
        assertEquals(3, metaMap.getSetterNames().length);

        Map<String, String> name = (Map<String, String>) metaMap.getValue("name");
        Map<String, String> address = (Map<String, String>) metaMap.getValue("address");

        assertEquals("Clinton", name.get("first"));
        assertEquals("1 Some Street", address.get("street"));
    }

    @Test
    public void shouldDemonstrateNullValueInMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        MetaObject metaMap = SystemMetaObject.forObject(map);
        assertFalse(metaMap.hasGetter("phone.home"));

        metaMap.setValue("phone", null);
        assertTrue(metaMap.hasGetter("phone"));
        // hasGetter returns true if the parent exists and is null.
        assertTrue(metaMap.hasGetter("phone.home"));
        assertTrue(metaMap.hasGetter("phone.home.ext"));
        assertNull(metaMap.getValue("phone"));
        assertNull(metaMap.getValue("phone.home"));
        assertNull(metaMap.getValue("phone.home.ext"));

        metaMap.setValue("phone.office", "789");
        assertFalse(metaMap.hasGetter("phone.home"));
        assertFalse(metaMap.hasGetter("phone.home.ext"));
        assertEquals("789", metaMap.getValue("phone.office"));
        assertNotNull(metaMap.getValue("phone"));
        assertNull(metaMap.getValue("phone.home"));
    }

    @Test
    public void shouldNotUseObjectWrapperFactoryByDefault() {
        MetaObject meta = SystemMetaObject.forObject(new Author());
        assertTrue(!meta.getObjectWrapper().getClass().equals(CustomBeanWrapper.class));
    }

    @Test
    public void shouldUseObjectWrapperFactoryWhenSet() {
        MetaObject meta = MetaObject.forObject(new Author(), SystemMetaObject.DEFAULT_OBJECT_FACTORY, new CustomBeanWrapperFactory(), new DefaultReflectorFactory());
        assertTrue(meta.getObjectWrapper().getClass().equals(CustomBeanWrapper.class));

        // Make sure the old default factory is in place and still works
        meta = SystemMetaObject.forObject(new Author());
        assertFalse(meta.getObjectWrapper().getClass().equals(CustomBeanWrapper.class));
    }

    @Test
    public void shouldMethodHasGetterReturnTrueWhenListElementSet() {
        List<Object> param1 = new ArrayList<Object>();
        param1.add("firstParam");
        param1.add(222);
        param1.add(new Date());

        Map<String, Object> parametersEmulation = new HashMap<String, Object>();
        parametersEmulation.put("param1", param1);
        parametersEmulation.put("filterParams", param1);

        MetaObject meta = SystemMetaObject.forObject(parametersEmulation);

        assertEquals(param1.get(0), meta.getValue("filterParams[0]"));
        assertEquals(param1.get(1), meta.getValue("filterParams[1]"));
        assertEquals(param1.get(2), meta.getValue("filterParams[2]"));

        assertTrue(meta.hasGetter("filterParams[0]"));
        assertTrue(meta.hasGetter("filterParams[1]"));
        assertTrue(meta.hasGetter("filterParams[2]"));
    }

}
