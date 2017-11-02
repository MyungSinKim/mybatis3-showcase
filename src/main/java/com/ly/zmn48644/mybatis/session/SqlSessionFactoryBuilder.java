
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
 * 思考为什么有这么多中不同传入参数的build方法.
 * 原因在于 XMLConfigBuilder的构造方法有三个参数,后两个参数是可选的.
 * 还有读取的配置文件也有不同类型 比如 reader 或者 inputstream
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
                //忽略此处异常
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
            //TODO 异常处理机制待深入
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            //TODO 异常处理机制待深入
            ErrorContext.instance().reset();
            try {
                inputStream.close();
            } catch (IOException e) {
                //忽略此处异常
            }
        }
    }

    /**
     * 真正构建 SqlSessionFactory 实现类的方法
     *
     * @param config
     * @return
     */
    public SqlSessionFactory build(Configuration config) {
        //根据 config 对象创建 SqlSessionFactory
        //下面是构建 SqlSessionFactory 的核心方法
        //构造方法传入 全局配置对象 config
        // DefaultSqlSessionFactory 对象创建完成之后 MyBatis 的初始化工作就已经完成了.
        // 也就是说 MyBatis 初始化的主要工作就是下面两步
        // 第一 创建全局配置对象,其中涉及到各种组件的初始化工作.
        // 第二 创建 DefaultSqlSessionFactory 对象
        return new DefaultSqlSessionFactory(config);
    }

}
