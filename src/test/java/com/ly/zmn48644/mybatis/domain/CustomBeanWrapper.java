
package com.ly.zmn48644.mybatis.domain;


import com.ly.zmn48644.mybatis.reflection.MetaObject;
import com.ly.zmn48644.mybatis.reflection.warpper.BeanWrapper;

public class CustomBeanWrapper extends BeanWrapper {
  public CustomBeanWrapper(MetaObject metaObject, Object object) {
    super(metaObject, object);
  }
}
