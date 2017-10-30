
package com.ly.zmn48644.mybatis.annotations;


import com.ly.zmn48644.mybatis.mapping.ResultSetType;
import com.ly.zmn48644.mybatis.mapping.StatementType;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Options {
    /**
     * The options for the {@link Options#flushCache()}.
     * The default is {@link FlushCachePolicy#DEFAULT}
     */
    public enum FlushCachePolicy {
        /**
         * <code>false</code> for select statement; <code>true</code> for insert/update/delete statement.
         */
        DEFAULT,
        /**
         * Flushes cache regardless of the statement type.
         */
        TRUE,
        /**
         * Does not flush cache regardless of the statement type.
         */
        FALSE
    }

    boolean useCache() default true;

    FlushCachePolicy flushCache() default FlushCachePolicy.DEFAULT;

    ResultSetType resultSetType() default ResultSetType.FORWARD_ONLY;

    StatementType statementType() default StatementType.PREPARED;

    int fetchSize() default -1;

    int timeout() default -1;

    boolean useGeneratedKeys() default false;

    String keyProperty() default "id";

    String keyColumn() default "";

    String resultSets() default "";
}
