
package com.ly.zmn48644.mybatis.session;


import com.ly.zmn48644.mybatis.logging.Log;
import com.ly.zmn48644.mybatis.logging.LogFactory;
import com.ly.zmn48644.mybatis.mapping.MappedStatement;

public enum AutoMappingUnknownColumnBehavior {


    NONE {
        @Override
        public void doAction(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
            // do nothing
        }
    },


    WARNING {
        @Override
        public void doAction(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
            log.warn(buildMessage(mappedStatement, columnName, property, propertyType));
        }
    },


    FAILING {
        @Override
        public void doAction(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
            throw new SqlSessionException(buildMessage(mappedStatement, columnName, property, propertyType));
        }
    };


    private static final Log log = LogFactory.getLog(AutoMappingUnknownColumnBehavior.class);


    public abstract void doAction(MappedStatement mappedStatement, String columnName, String propertyName, Class<?> propertyType);


    private static String buildMessage(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
        return new StringBuilder("Unknown column is detected on '")
                .append(mappedStatement.getId())
                .append("' auto-mapping. Mapping parameters are ")
                .append("[")
                .append("columnName=").append(columnName)
                .append(",").append("propertyName=").append(property)
                .append(",").append("propertyType=").append(propertyType != null ? propertyType.getName() : null)
                .append("]")
                .toString();
    }

}
