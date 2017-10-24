
package com.ly.zmn48644.mybatis.datasource;


import com.ly.zmn48644.mybatis.exceptions.PersistenceException;


public class DataSourceException extends PersistenceException {

    private static final long serialVersionUID = -5251396250407091334L;

    public DataSourceException() {
        super();
    }

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceException(Throwable cause) {
        super(cause);
    }

}
