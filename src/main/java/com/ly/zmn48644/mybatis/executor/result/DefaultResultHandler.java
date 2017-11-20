
package com.ly.zmn48644.mybatis.executor.result;


import com.ly.zmn48644.mybatis.reflection.factory.ObjectFactory;
import com.ly.zmn48644.mybatis.session.ResultContext;
import com.ly.zmn48644.mybatis.session.ResultHandler;

import java.util.ArrayList;
import java.util.List;


public class DefaultResultHandler implements ResultHandler<Object> {

    private final List<Object> list;

    public DefaultResultHandler() {
        list = new ArrayList<Object>();
    }

    @SuppressWarnings("unchecked")
    public DefaultResultHandler(ObjectFactory objectFactory) {
        list = objectFactory.create(List.class);
    }

    @Override
    public void handleResult(ResultContext<? extends Object> context) {
        list.add(context.getResultObject());
    }

    public List<Object> getResultList() {
        return list;
    }

}
