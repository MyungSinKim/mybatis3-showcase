
package com.ly.zmn48644.mybatis.transaction.jdbc;


import com.ly.zmn48644.mybatis.session.TransactionIsolationLevel;
import com.ly.zmn48644.mybatis.transaction.Transaction;
import com.ly.zmn48644.mybatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;


public class JdbcTransactionFactory implements TransactionFactory {

    @Override
    public void setProperties(Properties props) {
    }

    @Override
    public Transaction newTransaction(Connection conn) {
        return new JdbcTransaction(conn);
    }

    @Override
    public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
        return new JdbcTransaction(ds, level, autoCommit);
    }
}
