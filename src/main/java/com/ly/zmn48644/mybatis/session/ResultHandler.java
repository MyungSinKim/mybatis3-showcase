
package com.ly.zmn48644.mybatis.session;


public interface ResultHandler<T> {

    void handleResult(ResultContext<? extends T> resultContext);

}
