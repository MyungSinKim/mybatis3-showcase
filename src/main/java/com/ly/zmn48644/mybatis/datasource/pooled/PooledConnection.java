
package com.ly.zmn48644.mybatis.datasource.pooled;


import com.ly.zmn48644.mybatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * PooledConnection 中封装了真正的 数据库连接对象 (Connection realConnection)
 * PooledConnection 实现了 InvocationHandler 接口 用于使用JDK实现动态代理
 * 通过动态代理,实现对数据库连接的控制
 */
class PooledConnection implements InvocationHandler {

    private static final String CLOSE = "close";

    private static final Class<?>[] IFACES = new Class<?>[]{Connection.class};


    //调用 connection.hashCode(); 获得的连接hash值.
    private final int hashCode;

    //当前 PooledConnection 中封装的 连接的来源.
    private final PooledDataSource dataSource;

    //真正的数据库连接.
    private final Connection realConnection;

    //数据库连接的代理对象.
    private final Connection proxyConnection;

    //从数据源中取出 当前连接 的时间戳.
    private long checkoutTimestamp;

    //当前连接被创建的时间戳
    private long createdTimestamp;

    //当前连接最后一次被使用的时间戳
    private long lastUsedTimestamp;

    //由url,用户名,密码计算出来的hash值,用于标识当前连接所在的连接池
    private int connectionTypeCode;

    //检测当前连接是否有效
    private boolean valid;


    /**
     * 构造方法
     *
     * @param connection 真实的数据库连接
     * @param dataSource 数据源对象
     */
    public PooledConnection(Connection connection, PooledDataSource dataSource) {
        //获取hashcode
        this.hashCode = connection.hashCode();
        //赋值真实连接
        this.realConnection = connection;
        //赋值数据源
        this.dataSource = dataSource;
        //赋值创建时间戳
        this.createdTimestamp = System.currentTimeMillis();
        //赋值最后一次修改时间戳
        this.lastUsedTimestamp = System.currentTimeMillis();
        //赋值是否可用为true
        this.valid = true;

        /**
         * 使用JDK提供的动态代理实现,创建代理对象.
         */
        this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);
    }

    /*
     * Invalidates the connection
     */
    public void invalidate() {
        valid = false;
    }

    /*
     * Method to see if the connection is usable
     *
     * @return True if the connection is usable
     */
    public boolean isValid() {
        return valid && realConnection != null && dataSource.pingConnection(this);
    }

    /*
     * Getter for the *real* connection that this wraps
     *
     * @return The connection
     */
    public Connection getRealConnection() {
        return realConnection;
    }

    /*
     * Getter for the proxy for the connection
     *
     * @return The proxy
     */
    public Connection getProxyConnection() {
        return proxyConnection;
    }

    /*
     * Gets the hashcode of the real connection (or 0 if it is null)
     *
     * @return The hashcode of the real connection (or 0 if it is null)
     */
    public int getRealHashCode() {
        return realConnection == null ? 0 : realConnection.hashCode();
    }

    /*
     * Getter for the connection type (based on url + user + password)
     *
     * @return The connection type
     */
    public int getConnectionTypeCode() {
        return connectionTypeCode;
    }

    /*
     * Setter for the connection type
     *
     * @param connectionTypeCode - the connection type
     */
    public void setConnectionTypeCode(int connectionTypeCode) {
        this.connectionTypeCode = connectionTypeCode;
    }

    /*
     * Getter for the time that the connection was created
     *
     * @return The creation timestamp
     */
    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    /*
     * Setter for the time that the connection was created
     *
     * @param createdTimestamp - the timestamp
     */
    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    /*
     * Getter for the time that the connection was last used
     *
     * @return - the timestamp
     */
    public long getLastUsedTimestamp() {
        return lastUsedTimestamp;
    }

    /*
     * Setter for the time that the connection was last used
     *
     * @param lastUsedTimestamp - the timestamp
     */
    public void setLastUsedTimestamp(long lastUsedTimestamp) {
        this.lastUsedTimestamp = lastUsedTimestamp;
    }

    /*
     * Getter for the time since this connection was last used
     *
     * @return - the time since the last use
     */
    public long getTimeElapsedSinceLastUse() {
        return System.currentTimeMillis() - lastUsedTimestamp;
    }

    /*
     * Getter for the age of the connection
     *
     * @return the age
     */
    public long getAge() {
        return System.currentTimeMillis() - createdTimestamp;
    }

    /*
     * Getter for the timestamp that this connection was checked out
     *
     * @return the timestamp
     */
    public long getCheckoutTimestamp() {
        return checkoutTimestamp;
    }

    /*
     * Setter for the timestamp that this connection was checked out
     *
     * @param timestamp the timestamp
     */
    public void setCheckoutTimestamp(long timestamp) {
        this.checkoutTimestamp = timestamp;
    }

    /*
     * Getter for the time that this connection has been checked out
     *
     * @return the time
     */
    public long getCheckoutTime() {
        return System.currentTimeMillis() - checkoutTimestamp;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /*
     * 根据真实的数据库连接
     * @param obj - 和当前连接比较的另一个连接
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PooledConnection) {
            return realConnection.hashCode() == (((PooledConnection) obj).realConnection.hashCode());
        } else if (obj instanceof Connection) {
            return hashCode == obj.hashCode();
        } else {
            return false;
        }
    }

    /**
     * 池化数据源的核心逻辑,当调用连接的 close 方法时, 将此数据源放回到数据源中.
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //获取调用的方法名
        String methodName = method.getName();
        //判断调用的是否是 close 方法
        if (CLOSE.hashCode() == methodName.hashCode() && CLOSE.equals(methodName)) {
            //如果是调用的 close 方法,返回到数据源中.
            dataSource.pushConnection(this);
            return null;
        } else {
            //如果调用的不是 close 方法.
            try {
                if (!Object.class.equals(method.getDeclaringClass())) {
                    // issue #579 toString() should never fail
                    // throw an SQLException instead of a Runtime
                    checkConnection();
                }
                //调用被代理对象 也就是真实的数据源的方法
                return method.invoke(realConnection, args);
            } catch (Throwable t) {
                //抛出异常,这里的异常处理待深入研究.
                throw ExceptionUtil.unwrapThrowable(t);
            }
        }
    }

    /**
     * 检查数据连接是否可用,否则抛出异常.
     *
     * @throws SQLException
     */
    private void checkConnection() throws SQLException {
        if (!valid) {
            throw new SQLException("Error accessing PooledConnection. Connection is invalid.");
        }
    }

}
