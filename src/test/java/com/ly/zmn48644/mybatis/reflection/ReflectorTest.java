package com.ly.zmn48644.mybatis.reflection;

import org.junit.Assert;
import org.junit.Test;

public class ReflectorTest {
    @Test
    public void testGetSetterType() {
        //创建反射工厂
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        //获取指定类的反射器
        Reflector reflector = reflectorFactory.findForClass(Section.class);
        //根据反射器获取指定字段 的set方法的参宿类型
        Assert.assertEquals(Long.class, reflector.getSetterType("id"));
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
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
