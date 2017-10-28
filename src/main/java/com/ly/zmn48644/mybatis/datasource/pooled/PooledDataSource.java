
package com.ly.zmn48644.mybatis.datasource.pooled;


import com.ly.zmn48644.mybatis.datasource.unpooled.UnpooledDataSource;
import com.ly.zmn48644.mybatis.logging.Log;
import com.ly.zmn48644.mybatis.logging.LogFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * 池化的数据源
 */
public class PooledDataSource implements DataSource {

    private static final Log log = LogFactory.getLog(PooledDataSource.class);
    //连接池状态类,数据源类 通过  PoolState 对象来管理连接的状态
    private final PoolState state = new PoolState(this);

    //用于生成真实连接的数据源对象.
    private final UnpooledDataSource dataSource;


    //池中最大活动连接数
    protected int poolMaximumActiveConnections = 10;
    //池中最大空闲连接数
    protected int poolMaximumIdleConnections = 5;

    //最大的 checkoutTime
    protected int poolMaximumCheckoutTime = 20000;

    //在无法获取到连接是 线程最大的等待时间
    protected int poolTimeToWait = 20000;


    protected int poolMaximumLocalBadConnectionTolerance = 3;

    //在测试连接是否可用时向数据库发送的语句
    protected String poolPingQuery = "NO PING QUERY SET";
    //是否允许发送测试SQL
    protected boolean poolPingEnabled;

    //当连接超过下面属性指定的时长没有使用时,发送测试SQL确定连接是否能使用.
    protected int poolPingConnectionsNotUsedFor;

    //生成标识连接池的hashCode
    private int expectedConnectionTypeCode;


    /**
     * 默认无参构造方法
     */
    public PooledDataSource() {
        //赋值,非池化数据源
        dataSource = new UnpooledDataSource();
    }

    public PooledDataSource(UnpooledDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public PooledDataSource(String driver, String url, String username, String password) {
        dataSource = new UnpooledDataSource(driver, url, username, password);
        expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
    }

    public PooledDataSource(String driver, String url, Properties driverProperties) {
        dataSource = new UnpooledDataSource(driver, url, driverProperties);
        expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
    }

    public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, String username, String password) {
        dataSource = new UnpooledDataSource(driverClassLoader, driver, url, username, password);
        expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
    }

    public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, Properties driverProperties) {
        dataSource = new UnpooledDataSource(driverClassLoader, driver, url, driverProperties);
        expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
    }


    @Override
    public Connection getConnection() throws SQLException {
        return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return popConnection(username, password).getProxyConnection();
    }

    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        DriverManager.setLogWriter(logWriter);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    public void setDriver(String driver) {
        dataSource.setDriver(driver);
        forceCloseAll();
    }

    public void setUrl(String url) {
        dataSource.setUrl(url);
        forceCloseAll();
    }

    public void setUsername(String username) {
        dataSource.setUsername(username);
        forceCloseAll();
    }

    public void setPassword(String password) {
        dataSource.setPassword(password);
        forceCloseAll();
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        dataSource.setAutoCommit(defaultAutoCommit);
        forceCloseAll();
    }

    public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
        dataSource.setDefaultTransactionIsolationLevel(defaultTransactionIsolationLevel);
        forceCloseAll();
    }

    public void setDriverProperties(Properties driverProps) {
        dataSource.setDriverProperties(driverProps);
        forceCloseAll();
    }

    /*
     * The maximum number of active connections
     *
     * @param poolMaximumActiveConnections The maximum number of active connections
     */
    public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
        this.poolMaximumActiveConnections = poolMaximumActiveConnections;
        forceCloseAll();
    }

    /*
     * The maximum number of idle connections
     *
     * @param poolMaximumIdleConnections The maximum number of idle connections
     */
    public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
        this.poolMaximumIdleConnections = poolMaximumIdleConnections;
        forceCloseAll();
    }

    /*
     * The maximum number of tolerance for bad connection happens in one thread
      * which are applying for new {@link PooledConnection}
     *
     * @param poolMaximumLocalBadConnectionTolerance
     * max tolerance for bad connection happens in one thread
     *
     * @since 3.4.5
     */
    public void setPoolMaximumLocalBadConnectionTolerance(
            int poolMaximumLocalBadConnectionTolerance) {
        this.poolMaximumLocalBadConnectionTolerance = poolMaximumLocalBadConnectionTolerance;
    }

    /*
     * The maximum time a connection can be used before it *may* be
     * given away again.
     *
     * @param poolMaximumCheckoutTime The maximum time
     */
    public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
        this.poolMaximumCheckoutTime = poolMaximumCheckoutTime;
        forceCloseAll();
    }

    /*
     * The time to wait before retrying to get a connection
     *
     * @param poolTimeToWait The time to wait
     */
    public void setPoolTimeToWait(int poolTimeToWait) {
        this.poolTimeToWait = poolTimeToWait;
        forceCloseAll();
    }

    /*
     * The query to be used to check a connection
     *
     * @param poolPingQuery The query
     */
    public void setPoolPingQuery(String poolPingQuery) {
        this.poolPingQuery = poolPingQuery;
        forceCloseAll();
    }

    /*
     * Determines if the ping query should be used.
     *
     * @param poolPingEnabled True if we need to check a connection before using it
     */
    public void setPoolPingEnabled(boolean poolPingEnabled) {
        this.poolPingEnabled = poolPingEnabled;
        forceCloseAll();
    }

    /*
     * If a connection has not been used in this many milliseconds, ping the
     * database to make sure the connection is still good.
     *
     * @param milliseconds the number of milliseconds of inactivity that will trigger a ping
     */
    public void setPoolPingConnectionsNotUsedFor(int milliseconds) {
        this.poolPingConnectionsNotUsedFor = milliseconds;
        forceCloseAll();
    }

    public String getDriver() {
        return dataSource.getDriver();
    }

    public String getUrl() {
        return dataSource.getUrl();
    }

    public String getUsername() {
        return dataSource.getUsername();
    }

    public String getPassword() {
        return dataSource.getPassword();
    }

    public boolean isAutoCommit() {
        return dataSource.isAutoCommit();
    }

    public Integer getDefaultTransactionIsolationLevel() {
        return dataSource.getDefaultTransactionIsolationLevel();
    }

    public Properties getDriverProperties() {
        return dataSource.getDriverProperties();
    }

    public int getPoolMaximumActiveConnections() {
        return poolMaximumActiveConnections;
    }

    public int getPoolMaximumIdleConnections() {
        return poolMaximumIdleConnections;
    }

    public int getPoolMaximumLocalBadConnectionTolerance() {
        return poolMaximumLocalBadConnectionTolerance;
    }

    public int getPoolMaximumCheckoutTime() {
        return poolMaximumCheckoutTime;
    }

    public int getPoolTimeToWait() {
        return poolTimeToWait;
    }

    public String getPoolPingQuery() {
        return poolPingQuery;
    }

    public boolean isPoolPingEnabled() {
        return poolPingEnabled;
    }

    public int getPoolPingConnectionsNotUsedFor() {
        return poolPingConnectionsNotUsedFor;
    }

    /*
     * Closes all active and idle connections in the pool
     * 关闭所有的 连接池中的 活动的和空闲的连接
     * 当设置了数据源参数的时候就会调用此方法.
     */
    public void forceCloseAll() {
        //同步方法
        synchronized (state) {

            //更新当前连接池的标识
            expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
            //遍历所有的活动连接
            for (int i = state.activeConnections.size(); i > 0; i--) {
                try {

                    //获取连接
                    PooledConnection conn = state.activeConnections.remove(i - 1);
                    //将连接设置为不可用
                    conn.invalidate();
                    //得到真实的连接
                    Connection realConn = conn.getRealConnection();
                    if (!realConn.getAutoCommit()) {
                        realConn.rollback();
                    }
                    //关闭此连接
                    realConn.close();
                } catch (Exception e) {
                    // ignore
                }
            }

            //遍历所有的空闲连接
            for (int i = state.idleConnections.size(); i > 0; i--) {
                try {
                    PooledConnection conn = state.idleConnections.remove(i - 1);
                    //设置为不可用
                    conn.invalidate();
                    //获取倒真实连接
                    Connection realConn = conn.getRealConnection();
                    if (!realConn.getAutoCommit()) {
                        realConn.rollback();
                    }
                    //关闭空闲连接
                    realConn.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("PooledDataSource forcefully closed/removed all connections.");
        }
    }

    public PoolState getPoolState() {
        return state;
    }

    /**
     * 生成连接池的标识
     *
     * @param url
     * @param username
     * @param password
     * @return
     */
    private int assembleConnectionTypeCode(String url, String username, String password) {
        return ("" + url + username + password).hashCode();
    }


    /**
     * 归还连接到数据源
     *
     * @param conn
     * @throws SQLException
     */
    protected void pushConnection(PooledConnection conn) throws SQLException {

        //同步代码块使用 state对象 作为锁.
        synchronized (state) {
            //将归还的连接 从 活动连接数组中移除
            state.activeConnections.remove(conn);
            //判断连接是否可用
            if (conn.isValid()) {
                //如果 空闲连接数量 小于 配置的最大空闲连接数 并且 通过 getConnectionTypeCode 方法判断 此连接是否是此 DataSource的连接.
                if (state.idleConnections.size() < poolMaximumIdleConnections && conn.getConnectionTypeCode() == expectedConnectionTypeCode) {

                    state.accumulatedCheckoutTime += conn.getCheckoutTime();
                    if (!conn.getRealConnection().getAutoCommit()) {
                        conn.getRealConnection().rollback();
                    }
                    //根据返还的连接创建新的 PooledConnection 对象.
                    PooledConnection newConn = new PooledConnection(conn.getRealConnection(), this);

                    //将此次返还的连接重新放入空闲连接集合中
                    state.idleConnections.add(newConn);
                    //更新创建时间戳
                    newConn.setCreatedTimestamp(conn.getCreatedTimestamp());
                    //更新新创建的连接代理对象的最后使用时间
                    newConn.setLastUsedTimestamp(conn.getLastUsedTimestamp());
                    //将原来的代理对象设置为不可用.
                    conn.invalidate();
                    if (log.isDebugEnabled()) {
                        log.debug("Returned connection " + newConn.getRealHashCode() + " to pool.");
                    }
                    //唤醒在state上阻塞的 获取数据源的线程
                    state.notifyAll();
                } else {
                    //空闲连接数 大于等于 配置的最大空闲连接数
                    state.accumulatedCheckoutTime += conn.getCheckoutTime();

                    if (!conn.getRealConnection().getAutoCommit()) {
                        conn.getRealConnection().rollback();
                    }
                    //关闭此连接
                    conn.getRealConnection().close();

                    if (log.isDebugEnabled()) {
                        log.debug("Closed connection " + conn.getRealHashCode() + ".");
                    }
                    //将此连接设置为失效
                    conn.invalidate();
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("A bad connection (" + conn.getRealHashCode() + ") attempted to return to the pool, discarding connection.");
                }
                state.badConnectionCount++;
            }
        }
    }

    /**
     * 从数据源中获取连接
     *
     * @param username
     * @param password
     * @return
     * @throws SQLException
     */
    private PooledConnection popConnection(String username, String password) throws SQLException {
        boolean countedWait = false;
        PooledConnection conn = null;
        //获取开始获取连接时刻的时间戳.
        long t = System.currentTimeMillis();
        int localBadConnectionCount = 0;

        //这里采用循环的策略
        while (conn == null) {
            //同步代码块,获取的时候,归还方法会阻塞.
            synchronized (state) {
                //判断空闲连接集合是否为空
                if (!state.idleConnections.isEmpty()) {
                    //空闲连接池不为空
                    //移除并且获取第一个连接
                    conn = state.idleConnections.remove(0);
                    //判断日志级别,如果是debug级别则打印日志
                    if (log.isDebugEnabled()) {
                        log.debug("Checked out connection " + conn.getRealHashCode() + " from pool.");
                    }
                } else {
                    //进入此分支说明 空闲连接集合不为空

                    //判断活动连接数量是否小于 连接池配置的 最大活动连接数.
                    if (state.activeConnections.size() < poolMaximumActiveConnections) {
                        //如果当前活动连接数小于 配置的最大连接数则 可以创建新的连接
                        //调用数据源对象(非池化数据源)获取新的连接
                        conn = new PooledConnection(dataSource.getConnection(), this);
                        if (log.isDebugEnabled()) {
                            log.debug("Created connection " + conn.getRealHashCode() + ".");
                        }
                    } else {
                        //如果当前活动连接数 已经大于或者等于 配置的 最大活动连接数.
                        //不会创建新的连接
                        //取出活动连接集合中的第一个也就是最先创建的活跃连接
                        PooledConnection oldestActiveConnection = state.activeConnections.get(0);

                        //获取使用时间最长的活动连接，并计算使用的时间
                        long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
                        //判断是否超出了最大可回收时间
                        if (longestCheckoutTime > poolMaximumCheckoutTime) {
                            //超出则
                            //回收过期次数增加
                            state.claimedOverdueConnectionCount++;
                            //累计超时连接时间自增本次时间
                            state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
                            state.accumulatedCheckoutTime += longestCheckoutTime;

                            //从活动连接中移除超时连接
                            state.activeConnections.remove(oldestActiveConnection);

                            //判断是否是自动提交事务
                            if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {
                                try {
                                    // 如果不是自动提交事务，则将其回滚，因为可能存在一些操作
                                    oldestActiveConnection.getRealConnection().rollback();
                                } catch (SQLException e) {
                                    //失效连接不能被回滚
                                    log.debug("Bad connection. Could not roll back");
                                }
                            }
                            //将 逾期的连接 用新的代理类封装
                            conn = new PooledConnection(oldestActiveConnection.getRealConnection(), this);

                            //设置创建时间戳
                            conn.setCreatedTimestamp(oldestActiveConnection.getCreatedTimestamp());
                            //设置最后一次修改时间戳
                            conn.setLastUsedTimestamp(oldestActiveConnection.getLastUsedTimestamp());
                            //将之前逾期 的连接代理对象 设置为无效.

                            oldestActiveConnection.invalidate();
                            if (log.isDebugEnabled()) {
                                log.debug("Claimed overdue connection " + conn.getRealHashCode() + ".");
                            }
                        } else {
                            //进入到此分支则 获取连接线程必须 调用wait方法等待
                            // Must wait
                            try {
                                if (!countedWait) {
                                    //等待次数加一
                                    state.hadToWaitCount++;
                                    countedWait = true;
                                }
                                if (log.isDebugEnabled()) {
                                    log.debug("Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
                                }
                                long wt = System.currentTimeMillis();
                                //获取线程进入等待, 等待的最大时长为 配置的 poolTimeToWait.
                                state.wait(poolTimeToWait);
                                //在总的累计等待时长上加上本次等待的时长.
                                state.accumulatedWaitTime += System.currentTimeMillis() - wt;
                            } catch (InterruptedException e) {
                                //进入下一次获取循环.
                                break;
                            }
                        }
                    }
                }

                //如果在上面的步骤中获取到了连接
                if (conn != null) {
                    // ping to server and check the connection is valid or not
                    //判断连接是否可用
                    if (conn.isValid()) {
                        //如果可用,并且不自动提交
                        if (!conn.getRealConnection().getAutoCommit()) {
                            //调用连接的 rollback 方法.
                            //这里可能和连接被重复使用有关.
                            conn.getRealConnection().rollback();
                        }
                        //刷新配置属性
                        conn.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(), username, password));
                        //连接的获取时间
                        conn.setCheckoutTimestamp(System.currentTimeMillis());
                        //连接的最后修改时间
                        conn.setLastUsedTimestamp(System.currentTimeMillis());
                        //将获取到的连接放入 活动连接结合
                        state.activeConnections.add(conn);
                        //连接请求数加一
                        state.requestCount++;
                        //累计获取请求数累加
                        state.accumulatedRequestTime += System.currentTimeMillis() - t;
                    } else {
                        //如果没有获取到可用的连接
                        if (log.isDebugEnabled()) {
                            log.debug("A bad connection (" + conn.getRealHashCode() + ") was returned from the pool, getting another connection.");
                        }
                        //不可用连接自增加一
                        state.badConnectionCount++;
                        localBadConnectionCount++;
                        //将 conn 设置为null
                        conn = null;

                        //如果本地坏连接数量 大于  最大空闲连接数 + (poolMaximumLocalBadConnectionTolerance 默认是 3)
                        //则抛出异常
                        if (localBadConnectionCount > (poolMaximumIdleConnections + poolMaximumLocalBadConnectionTolerance)) {
                            if (log.isDebugEnabled()) {
                                log.debug("PooledDataSource: Could not get a good connection to the database.");
                            }
                            throw new SQLException("PooledDataSource: Could not get a good connection to the database.");
                        }
                    }
                }
            }

        }

        if (conn == null) {
            if (log.isDebugEnabled()) {
                log.debug("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
            }
            throw new SQLException("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
        }

        return conn;
    }


    /**
     * 检查连接是否一直是可用的
     *
     * @param conn
     * @return
     */
    protected boolean pingConnection(PooledConnection conn) {
        //定义检查是否可用过
        boolean result = true;
        try {
            //调用真实数据库连接的 isClosed 方法判断是否关闭
            //如果此语句执行报错,说明不可用,跳入异常处理逻辑,返回此连接是不可用的.
            result = !conn.getRealConnection().isClosed();
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
            }
            result = false;
        }

        if (result) {
            //如果没有关闭
            if (poolPingEnabled) {
                //如果配置了检查为true进入

                //长时间未使用的连接才需要进行执行测试SQL来检测可用性
                // 超过 poolPingConnectionsNotUsedFor 设定的时间会检测
                if (poolPingConnectionsNotUsedFor >= 0 && conn.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor) {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("Testing connection " + conn.getRealHashCode() + " ...");
                        }
                        Connection realConn = conn.getRealConnection();
                        Statement statement = realConn.createStatement();
                        //执行测试SQL 语句
                        ResultSet rs = statement.executeQuery(poolPingQuery);
                        rs.close();
                        statement.close();
                        if (!realConn.getAutoCommit()) {
                            realConn.rollback();
                        }
                        //如果执行到这里没有报错的话说明此连接是可用的.
                        //返回结果说明连接是可用的
                        result = true;
                        if (log.isDebugEnabled()) {
                            log.debug("Connection " + conn.getRealHashCode() + " is GOOD!");
                        }
                    } catch (Exception e) {
                        log.warn("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
                        try {
                            conn.getRealConnection().close();
                        } catch (Exception e2) {
                            //ignore
                        }
                        result = false;
                        if (log.isDebugEnabled()) {
                            log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
                        }
                    }
                }
            }
        }
        return result;
    }

    /*
     * Unwraps a pooled connection to get to the 'real' connection
     *
     * @param conn - the pooled connection to unwrap
     * @return The 'real' connection
     */
    public static Connection unwrapConnection(Connection conn) {
        if (Proxy.isProxyClass(conn.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(conn);
            if (handler instanceof PooledConnection) {
                return ((PooledConnection) handler).getRealConnection();
            }
        }
        return conn;
    }

    protected void finalize() throws Throwable {
        forceCloseAll();
        super.finalize();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // requires JDK version 1.6
    }

}
