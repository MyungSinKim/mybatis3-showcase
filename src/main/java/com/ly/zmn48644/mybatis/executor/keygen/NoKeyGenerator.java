
package com.ly.zmn48644.mybatis.executor.keygen;


import com.ly.zmn48644.mybatis.mapping.MappedStatement;

import java.sql.Statement;
import java.util.concurrent.Executor;


public class NoKeyGenerator implements KeyGenerator {

    public static final NoKeyGenerator INSTANCE = new NoKeyGenerator();

    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // Do Nothing
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // Do Nothing
    }

}
