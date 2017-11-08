
package com.ly.zmn48644.mybatis.builder.annotation;

import com.ly.zmn48644.mybatis.annotations.*;
import com.ly.zmn48644.mybatis.builder.IncompleteElementException;
import com.ly.zmn48644.mybatis.builder.MapperBuilderAssistant;
import com.ly.zmn48644.mybatis.builder.XMLMapperBuilder;
import com.ly.zmn48644.mybatis.io.Resources;
import com.ly.zmn48644.mybatis.session.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Clinton Begin
 */
public class MapperAnnotationBuilder {

    private final Set<Class<? extends Annotation>> sqlAnnotationTypes = new HashSet<Class<? extends Annotation>>();
    private final Set<Class<? extends Annotation>> sqlProviderAnnotationTypes = new HashSet<Class<? extends Annotation>>();

    //全局唯一配置对象
    private final Configuration configuration;
    //每个 mapper 对应一个 自己的 MapperAnnotationBuilder 对象.
    //每个 MapperAnnotationBuilder 对象对应一个自己独有的MapperBuilderAssistant对象.
    private final MapperBuilderAssistant assistant;
    private final Class<?> type;

    public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
        String resource = type.getName().replace('.', '/') + ".java (best guess)";
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;

        sqlAnnotationTypes.add(Select.class);
        sqlAnnotationTypes.add(Insert.class);
        sqlAnnotationTypes.add(Update.class);
        sqlAnnotationTypes.add(Delete.class);

        sqlProviderAnnotationTypes.add(SelectProvider.class);
        sqlProviderAnnotationTypes.add(InsertProvider.class);
        sqlProviderAnnotationTypes.add(UpdateProvider.class);
        sqlProviderAnnotationTypes.add(DeleteProvider.class);
    }


    /**
     * 解析通过注解配置的SQL信息
     */
    public void parse() {
        String resource = type.toString();
        if (!configuration.isResourceLoaded(resource)) {
            //首先去尝试加载XML配置资源
            loadXmlResource();

            configuration.addLoadedResource(resource);
            assistant.setCurrentNamespace(type.getName());
            parseCache();
            parseCacheRef();
            Method[] methods = type.getMethods();
            for (Method method : methods) {
                try {
                    // issue #237
                    if (!method.isBridge()) {
                        parseStatement(method);
                    }
                } catch (IncompleteElementException e) {
                    configuration.addIncompleteMethod(new MethodResolver(this, method));
                }
            }
        }
        parsePendingMethods();
    }

    /**
     * 根据命名空间加载XML资源
     */
    private void loadXmlResource() {
        //判断此命名空间为key的资源是否加载过
        if (!configuration.isResourceLoaded("namespace:" + type.getName())) {
            //如果没有加载过
            //根据命名空间 经过 替换后得到XML文件所在的位置
            String xmlResource = type.getName().replace('.', '/') + ".xml";
            InputStream inputStream = null;
            try {
                //读取xml文件
                inputStream = Resources.getResourceAsStream(type.getClassLoader(), xmlResource);
            } catch (IOException e) {
                // ignore, resource is not required
            }
            if (inputStream != null) {
                //如果读取到了XML 解析XML
                XMLMapperBuilder xmlParser = new XMLMapperBuilder(inputStream, assistant.getConfiguration(), xmlResource, configuration.getSqlFragments(), type.getName());
                xmlParser.parse();
            }
        }
    }

    private void parsePendingMethods() {
    }

    void parseStatement(Method method) {

    }

    private void parseCacheRef() {
    }

    private void parseCache() {
    }

}
