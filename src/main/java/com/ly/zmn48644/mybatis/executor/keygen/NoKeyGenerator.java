
package com.ly.zmn48644.mybatis.executor.keygen;


import com.ly.zmn48644.mybatis.mapping.MappedStatement;

import java.sql.Statement;
import java.util.concurrent.Executor;


public class NoKeyGenerator implements KeyGenerator {

    public static final NoKeyGenerator INSTANCE = new NoKeyGenerator();


    @Override
    public void processBefore(com.ly.zmn48644.mybatis.executor.Executor executor, MappedStatement ms, Statement stmt, Object parameter) {

    }

    @Override
    public void processAfter(com.ly.zmn48644.mybatis.executor.Executor executor, MappedStatement ms, Statement stmt, Object parameter) {

    }
}
