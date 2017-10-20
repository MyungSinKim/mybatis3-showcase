
package com.ly.zmn48644.mybatis.session;

import java.sql.Connection;

public interface SqlSessionFactory {

    SqlSession openSession();

    SqlSession openSession(boolean autoCommit);

    SqlSession openSession(Connection connection);

    //TODO 临时注释 这部分 设计到  事务管理 , 执行器 模块
//  SqlSession openSession(TransactionIsolationLevel level);
//
//  SqlSession openSession(ExecutorType execType);
//  SqlSession openSession(ExecutorType execType, boolean autoCommit);
//  SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level);
//  SqlSession openSession(ExecutorType execType, Connection connection);

    Configuration getConfiguration();

}
