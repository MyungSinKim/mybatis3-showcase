package com.ly.zmn48644.mybatis.reflection;

import com.ly.zmn48644.mybatis.reflection.invoker.Invoker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.List;

public class ReflectorTest {
    Reflector reflector;

    @Before
    public void setup() {
        //创建反射工厂
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        //获取指定类的反射器
        reflector = reflectorFactory.findForClass(Section.class);
    }


    @Test
    public void testGetSetterType() {
        //根据反射器获取指定字段 的set方法的参数类型
        Assert.assertEquals(Long.class, reflector.getSetterType("id"));
    }

    @Test
    public void getType() {
        Class<?> type = reflector.getType();
        System.out.println(type.toString());
        //获取通用toString方法->GE(General electric)
        System.out.println(type.toGenericString());
    }

    @Test
    public void getDefaultConstructor() {
        //获取默认的构造方法
        Constructor<?> defaultConstructor = reflector.getDefaultConstructor();
        System.out.println(defaultConstructor.toGenericString());
    }

    @Test
    public void hasDefaultConstructor() {
        //判断是否存在默认无参构造方法
        boolean hasDefaultConstructor = reflector.hasDefaultConstructor();
        System.out.println(hasDefaultConstructor);
    }

    @Test
    public void getSetInvoker() {
        //获取set方法调用器,根据属性名获取调用器,这里不支持属性表达式功能.
        //属性表达式的功能在MetaClass中提供.
        Invoker setInvoker = reflector.getSetInvoker("user");
        System.out.println(setInvoker.toString());
    }

    @Test
    public void getGetInvoker() {
        //获取类的get方法调用器,根据属性名获取调用器,不支持属性表达式
        Invoker invoker = reflector.getGetInvoker("user");
        System.out.println(invoker.toString());
    }

    @Test
    public void getSetterType() {
        //获取set方法的入参类型,这个使用场景存疑?
        Class<?> setterType = reflector.getSetterType("user");
        System.out.println(setterType);
    }

    @Test
    public void getGetterType() {
        //用于获取get方法的返回值类型
        Class<?> user = reflector.getGetterType("user");
        System.out.println(user);
    }

    @Test
    public void getGetablePropertyNames() {
        //获取可以获取到的javabean的字段,也就是存在get方法的属性列表.
        String[] getablePropertyNames = reflector.getGetablePropertyNames();
        for (String s : getablePropertyNames) {
            System.out.println(s);
        }
    }

    @Test
    public void getSetablePropertyNames() {
        //获取可以设置的,也就是存在set方法的属性列表.
        String[] setablePropertyNames = reflector.getSetablePropertyNames();
        for (String setablePropertyName : setablePropertyNames) {
            System.out.println(setablePropertyName);
        }
    }

    @Test
    public void hasSetter() {
        //判断是否存在 set 方法.
        boolean user = reflector.hasSetter("user");
        System.out.println(user);
    }

    @Test
    public void hasGetter() {
        //判断是否存在get方法
        boolean user = reflector.hasGetter("user");
        System.out.println(user);
    }

    @Test
    public void findPropertyName() {
        String user = reflector.findPropertyName("user");
        System.out.println(user);
    }
}


/**
 * 实体接口
 */
interface Entity<T> {
    T getId();

    void setId(T id);
}

/**
 * 用于测试的抽象实体
 */
abstract class AbstractEntity implements Entity<Long> {

    private Long id;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
}

/**
 * 用于测试的部门测试类
 */
class Section extends AbstractEntity implements Entity<Long> {
    private String userName;
    private User user;
    private List<User> userList;

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

class User {
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}