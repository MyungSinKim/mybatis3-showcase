
package com.ly.zmn48644.mybatis.session;

/**
 * MyBatis 中针对 自动映射 的配置.
 */
public enum AutoMappingBehavior {

    /**
     * 禁用自动映射
     */
    NONE,

    /**
     * 只会自动映射非嵌套结果映射的这部分映射
     */
    PARTIAL,

    /**
     * 自动映射全部结果,包含嵌套结果集映射
     */
    FULL
}
