
package com.ly.zmn48644.mybatis.session;


import com.ly.zmn48644.mybatis.builder.xml.XMLConfigBuilder;
import com.ly.zmn48644.mybatis.exceptions.ExceptionFactory;
import com.ly.zmn48644.mybatis.executor.ErrorContext;
import com.ly.zmn48644.mybatis.session.defaults.DefaultSqlSessionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;


/**
 * 此类的build 方法就是mybatis的初始化入口.
 */
public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(Reader reader) {
        return build(reader, null, null);
    }

    public SqlSessionFactory build(Reader reader, String environment) {
        return build(reader, environment, null);
    }

    public SqlSessionFactory build(Reader reader, Properties properties) {
        return build(reader, null, properties);
    }


    /**
     * 初始化入口方法(重载核心方法)
     *
     * @param reader
     * @param environment
     * @param properties
     * @return
     */
    public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
        try {

            //这里使用了,建造者模式,将生成组件,和装配组件的逻辑分离开来.
            XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
            //调用 parser.parse方法返回 Configuration 配置对象.
            return build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                reader.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    public SqlSessionFactory build(InputStream inputStream) {
        return build(inputStream, null, null);
    }

    public SqlSessionFactory build(InputStream inputStream, String environment) {
        return build(inputStream, environment, null);
    }

    public SqlSessionFactory build(InputStream inputStream, Properties properties) {
        return build(inputStream, null, properties);
    }

    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        try {
            XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
            return build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                inputStream.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    public SqlSessionFactory build(Configuration config) {

        return new DefaultSqlSessionFactory(config);

    }

}
