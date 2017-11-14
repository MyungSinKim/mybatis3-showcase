
package com.ly.zmn48644.mybatis.executor.keygen;


import com.ly.zmn48644.mybatis.mapping.MappedStatement;

import java.sql.Statement;
import java.util.concurrent.Executor;

public interface KeyGenerator {

    void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

    void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

}
