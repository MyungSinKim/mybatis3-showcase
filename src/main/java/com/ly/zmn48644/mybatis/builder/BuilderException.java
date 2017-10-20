
package com.ly.zmn48644.mybatis.builder;

import com.ly.zmn48644.mybatis.exceptions.PersistenceException;

public class BuilderException extends PersistenceException {

    private static final long serialVersionUID = -3885164021020443281L;

    public BuilderException() {
        super();
    }

    public BuilderException(String message) {
        super(message);
    }

    public BuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuilderException(Throwable cause) {
        super(cause);
    }
}
