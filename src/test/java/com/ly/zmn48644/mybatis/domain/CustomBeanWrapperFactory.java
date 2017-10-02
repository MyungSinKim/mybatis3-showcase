
package com.ly.zmn48644.mybatis.domain;


import com.ly.zmn48644.mybatis.domain.blog.Author;
import com.ly.zmn48644.mybatis.reflection.MetaObject;
import com.ly.zmn48644.mybatis.reflection.warpper.ObjectWrapper;
import com.ly.zmn48644.mybatis.reflection.warpper.ObjectWrapperFactory;

public class CustomBeanWrapperFactory implements ObjectWrapperFactory {
    @Override
    public boolean hasWrapperFor(Object object) {
        if (object instanceof Author) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        return new CustomBeanWrapper(metaObject, object);
    }
}
