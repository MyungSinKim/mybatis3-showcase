/**
 * Copyright 2009-2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ly.zmn48644.mybatis.session.defaults;


import com.ly.zmn48644.mybatis.session.Configuration;
import com.ly.zmn48644.mybatis.session.SqlSession;
import com.ly.zmn48644.mybatis.session.SqlSessionFactory;

import java.sql.Connection;

/**
 * 默认的
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    public DefaultSqlSessionFactory(Configuration configuration) {
    }

    /**
     * 从 SqlSessionFactory 中获取一个 SqlSession
     *
     * @return
     */
    @Override
    public SqlSession openSession() {
        return null;
    }


    /**
     * 从 SqlSessionFactory 中获取一个 SqlSession
     * 提供指定是否自动提交功能
     *
     * @return
     */
    @Override
    public SqlSession openSession(boolean autoCommit) {
        return null;
    }

    /**
     * 从 SqlSessionFactory 中获取一个 SqlSession
     * 指定使用的真实数据库连接(Connection)
     *
     * @param connection
     * @return
     */
    @Override
    public SqlSession openSession(Connection connection) {
        return null;
    }

    /**
     * 获取全局配置方法
     *
     * @return
     */
    @Override
    public Configuration getConfiguration() {
        return null;
    }
}
