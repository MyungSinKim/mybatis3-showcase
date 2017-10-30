
package com.ly.zmn48644.mybatis.binding;


import com.ly.zmn48644.mybatis.exceptions.PersistenceException;


public class BindingException extends PersistenceException {

    private static final long serialVersionUID = 4300802238789381562L;

    public BindingException() {
        super();
    }

    public BindingException(String message) {
        super(message);
    }

    public BindingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BindingException(Throwable cause) {
        super(cause);
    }
}
