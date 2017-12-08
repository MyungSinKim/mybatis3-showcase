
package com.ly.zmn48644.mybatis.binding;


import com.ly.zmn48644.mybatis.annotations.Flush;
import com.ly.zmn48644.mybatis.annotations.MapKey;
import com.ly.zmn48644.mybatis.cursor.Cursor;
import com.ly.zmn48644.mybatis.mapping.MappedStatement;
import com.ly.zmn48644.mybatis.mapping.SqlCommandType;
import com.ly.zmn48644.mybatis.reflection.MetaObject;
import com.ly.zmn48644.mybatis.reflection.ParamNameResolver;
import com.ly.zmn48644.mybatis.reflection.TypeParameterResolver;
import com.ly.zmn48644.mybatis.session.Configuration;
import com.ly.zmn48644.mybatis.session.ResultHandler;
import com.ly.zmn48644.mybatis.session.RowBounds;
import com.ly.zmn48644.mybatis.session.SqlSession;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper接口中的一个方法 对应 一个 MapperMethod 对象
 */
public class MapperMethod {

    //SqlCommand 封装了,此方法对应的SQL操作的类型.
    private final SqlCommand command;

    //方法签名类, 用于封装 Mapper 接口中方法的元数据, 封装和处理 方法参数 和 返回值的信息
    private final MethodSignature method;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
        this.command = new SqlCommand(config, mapperInterface, method);
        this.method = new MethodSignature(config, mapperInterface, method);
    }

    /**
     * 接口代理类中会调用此方法
     *
     * @param sqlSession
     * @param args
     * @return
     */
    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result;
        //根据 type 进入不同的查询语句分支
        switch (command.getType()) {
            case INSERT: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.insert(command.getName(), param));
                break;
            }
            case UPDATE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.update(command.getName(), param));
                break;
            }
            case DELETE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.delete(command.getName(), param));
                break;
            }
            case SELECT:

                if (method.returnsVoid() && method.hasResultHandler()) {
                    //判断 方法是否是 void 返回 并且 方法中指定了 resulthandler
                    executeWithResultHandler(sqlSession, args);
                    //不返回数据
                    result = null;
                } else if (method.returnsMany()) {
                    //如果返回结果是多行记录,比如数组,集合.
                    result = executeForMany(sqlSession, args);
                } else if (method.returnsMap()) {
                    //如果返回结果是一个map对象
                    result = executeForMap(sqlSession, args);
                } else if (method.returnsCursor()) {
                    //返回结果是一个 游标查询对象
                    result = executeForCursor(sqlSession, args);
                } else {
                    //返回的是一个元素
                    //解析方法参数
                    Object param = method.convertArgsToSqlCommandParam(args);
                    //调用 sqlSession 中的方法执行SQL.
                    result = sqlSession.selectOne(command.getName(), param);
                }
                break;
            case FLUSH:
                result = sqlSession.flushStatements();
                break;
            default:
                throw new BindingException("Unknown execution method for: " + command.getName());
        }
        if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
            throw new BindingException("Mapper method '" + command.getName()
                    + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
        }
        return result;
    }

    private Object rowCountResult(int rowCount) {
        final Object result;
        if (method.returnsVoid()) {
            result = null;
        } else if (Integer.class.equals(method.getReturnType()) || Integer.TYPE.equals(method.getReturnType())) {
            result = rowCount;
        } else if (Long.class.equals(method.getReturnType()) || Long.TYPE.equals(method.getReturnType())) {
            result = (long) rowCount;
        } else if (Boolean.class.equals(method.getReturnType()) || Boolean.TYPE.equals(method.getReturnType())) {
            result = rowCount > 0;
        } else {
            throw new BindingException("Mapper method '" + command.getName() + "' has an unsupported return type: " + method.getReturnType());
        }
        return result;
    }

    private void executeWithResultHandler(SqlSession sqlSession, Object[] args) {
        MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(command.getName());
        if (void.class.equals(ms.getResultMaps().get(0).getType())) {
            throw new BindingException("method " + command.getName()
                    + " needs either a @ResultMap annotation, a @ResultType annotation,"
                    + " or a resultType attribute in XML so a ResultHandler can be used as a parameter.");
        }
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            sqlSession.select(command.getName(), param, rowBounds, method.extractResultHandler(args));
        } else {
            sqlSession.select(command.getName(), param, method.extractResultHandler(args));
        }
    }

    /**
     * 执行返回多行结果的查询语句
     *
     * @param sqlSession
     * @param args
     * @param <E>
     * @return
     */
    private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
        List<E> result;
        //将接口方法参数转换为一个map或者 如果只有一个参数的话 返回 参数值.
        Object param = method.convertArgsToSqlCommandParam(args);
        //接口方法参数中是否存在 RowBounds 参数.
        if (method.hasRowBounds()) {
            //如果存在, 从方法参数中获取 rowBounds
            RowBounds rowBounds = method.extractRowBounds(args);
            //调用 sqlSession 的 selectList 接口查询结果
            result = sqlSession.<E>selectList(command.getName(), param, rowBounds);
        } else {
            result = sqlSession.<E>selectList(command.getName(), param);
        }
        //如果方法的返回值 不是List,二十数组或者其他类型的集合
        //则需要将list 转换成 返回结果类型的对象
        if (!method.getReturnType().isAssignableFrom(result.getClass())) {
            if (method.getReturnType().isArray()) {
                //返回类型 是一个数组
                //将 result 转换为数组
                return convertToArray(result);
            } else {
                //将 result 转为 collection
                return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
            }
        }
        return result;
    }

    private <T> Cursor<T> executeForCursor(SqlSession sqlSession, Object[] args) {
        Cursor<T> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.<T>selectCursor(command.getName(), param, rowBounds);
        } else {
            result = sqlSession.<T>selectCursor(command.getName(), param);
        }
        return result;
    }

    /**
     * 根据 method.getReturnType() 的类型创建 此类型的Collection对象
     * 并且将参数中的list 添加到创建的集合中 返回.
     *
     * @param config
     * @param list
     * @param <E>
     * @return
     */
    private <E> Object convertToDeclaredCollection(Configuration config, List<E> list) {
        //使用 objectFactory 创建空的返回结果
        Object collection = config.getObjectFactory().create(method.getReturnType());
        //获取 collection 的元数据对象
        MetaObject metaObject = config.newMetaObject(collection);

        metaObject.addAll(list);
        return collection;
    }

    /**
     * 将 list 转换为 数组
     *
     * @param list
     * @param <E>
     * @return
     */
    private <E> Object convertToArray(List<E> list) {
        //获取数组中元素的类型
        Class<?> arrayComponentType = method.getReturnType().getComponentType();
        //创建数组, 传入的元素类型,和数组长度创建 一个空数组
        Object array = Array.newInstance(arrayComponentType, list.size());

        //如果list中的元素是基本类型
        if (arrayComponentType.isPrimitive()) {
            for (int i = 0; i < list.size(); i++) {
                //对每一个索引位置单独赋值.
                Array.set(array, i, list.get(i));
            }
            return array;
        } else {
            //如果list中的元素不是基本类型
            //调用list 的toArray方法转换为数组.
            return list.toArray((E[]) array);
        }
    }

    /**
     * 执行返回结果是一个map的情况
     *
     * @param sqlSession
     * @param args
     * @param <K>
     * @param <V>
     * @return
     */
    private <K, V> Map<K, V> executeForMap(SqlSession sqlSession, Object[] args) {
        Map<K, V> result;
        //转换接口参数为一个map.
        Object param = method.convertArgsToSqlCommandParam(args);
        //判断参数列表中是否 含有  RowBounds 参数
        if (method.hasRowBounds()) {
            //提取 rowBounds
            RowBounds rowBounds = method.extractRowBounds(args);
            //调用 sqlSession 方法执行
            result = sqlSession.<K, V>selectMap(command.getName(), param, method.getMapKey(), rowBounds);
        } else {
            result = sqlSession.<K, V>selectMap(command.getName(), param, method.getMapKey());
        }
        return result;
    }

    /**
     * 定义了一个静态内部类用于存储 接口参数名 和接口参数值得 对应关系.
     *
     * @param <V>
     */
    public static class ParamMap<V> extends HashMap<String, V> {
        private static final long serialVersionUID = -2212268410512043556L;

        @Override
        public V get(Object key) {
            //如果使用了 没有定义的 参数 这里会报异常
            //这种设计思路很不错.
            if (!super.containsKey(key)) {
                throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }

    }

    public static class SqlCommand {

        private final String name;
        private final SqlCommandType type;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            final String methodName = method.getName();
            final Class<?> declaringClass = method.getDeclaringClass();
            MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass,
                    configuration);
            if (ms == null) {
                if (method.getAnnotation(Flush.class) != null) {
                    name = null;
                    type = SqlCommandType.FLUSH;
                } else {
                    throw new BindingException("Invalid bound statement (not found): "
                            + mapperInterface.getName() + "." + methodName);
                }
            } else {
                name = ms.getId();
                type = ms.getSqlCommandType();
                if (type == SqlCommandType.UNKNOWN) {
                    throw new BindingException("Unknown execution method for: " + name);
                }
            }
        }

        public String getName() {
            return name;
        }

        public SqlCommandType getType() {
            return type;
        }

        /**
         * 给定接口class,方法名,以及声明类型,从 全局配置对象中查找 对应的 MappedStatement
         * 这里存在递归调用.
         * <p>
         * 通过 返回的 MappedStatement 对象可以获取到 type
         *
         * @param mapperInterface
         * @param methodName
         * @param declaringClass
         * @param configuration
         * @return
         */
        private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName,
                                                       Class<?> declaringClass, Configuration configuration) {

            //根据  接口名 + . + 方法名 作为ID,从configuration中获取
            String statementId = mapperInterface.getName() + "." + methodName;
            if (configuration.hasStatement(statementId)) {
                return configuration.getMappedStatement(statementId);
            } else if (mapperInterface.equals(declaringClass)) {
                return null;
            }
            for (Class<?> superInterface : mapperInterface.getInterfaces()) {
                if (declaringClass.isAssignableFrom(superInterface)) {
                    MappedStatement ms = resolveMappedStatement(superInterface, methodName,
                            declaringClass, configuration);
                    if (ms != null) {
                        return ms;
                    }
                }
            }
            return null;
        }
    }

    public static class MethodSignature {

        //接口方法 返回值是否是多元素的,比如是数组或者是集合
        private final boolean returnsMany;
        //接口方法 返回值是否是一个Map
        private final boolean returnsMap;
        //返回值 是否是 void
        private final boolean returnsVoid;
        //接口方法的返回值 是否是 游标类型
        private final boolean returnsCursor;
        //方法返回值类型
        private final Class<?> returnType;
        private final String mapKey;

        private final Integer resultHandlerIndex;
        private final Integer rowBoundsIndex;
        private final ParamNameResolver paramNameResolver;

        public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {

            //根据接口和方法 获取方法返回值
            //使用反射工具类获取接口中方法的返回值类型
            Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
            //设置 returnType 这里面有三种情况
            if (resolvedReturnType instanceof Class<?>) {
                //第一种情况返回的 是没有带泛型信息的 比如 int String User 等等
                this.returnType = (Class<?>) resolvedReturnType;
            } else if (resolvedReturnType instanceof ParameterizedType) {
                //第二种情况是 返回的是 参数化类型 比如  List<String> ,Set<String> 等等
                this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
            } else {
                //第三种情况 如果上面两种情况没有获得 返回值类型 则直接调用 方法的 getReturnType获取返回值类型
                //TODO 这里是有些疑问的.还不清楚 返回值 是那种类型会出现这种情况
                this.returnType = method.getReturnType();
            }

            //设置返回值是否是 void
            this.returnsVoid = void.class.equals(this.returnType);
            //设置返回值是否是数组或者集合类型
            this.returnsMany = (configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray());
            //设置返回值类型是否是游标类型
            this.returnsCursor = Cursor.class.equals(this.returnType);
            this.mapKey = getMapKey(method);
            this.returnsMap = (this.mapKey != null);
            //RowBounds 参数在方法参数列表中的位置
            this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
            //ResultHandler 参数在方法参数列表中的位置
            this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
            //方法参数解析器
            this.paramNameResolver = new ParamNameResolver(configuration, method);
        }


        /**
         * 完成 方法参数 到 Sql命令参数 的转换
         * <p>
         * 存在下列两种情况
         * <p>
         * 只有一个参数或者有多个参数.
         * <p>
         * 如果只有一个则参数 直接返回 此参数值
         * 如果有多个参数 返回 一个Map 例如 {name=zmn, param1=zmn, age=27, param2=27}
         * <p>
         * 如果想获取真实的 方法参数名 需要JDK1.8 并且要添加 编译参数 -parameters
         *
         * @param args
         * @return
         */
        public Object convertArgsToSqlCommandParam(Object[] args) {
            //调用反射工具方法转换参数
            return paramNameResolver.getNamedParams(args);
        }

        public boolean hasRowBounds() {
            return rowBoundsIndex != null;
        }

        public RowBounds extractRowBounds(Object[] args) {
            return hasRowBounds() ? (RowBounds) args[rowBoundsIndex] : null;
        }

        public boolean hasResultHandler() {
            return resultHandlerIndex != null;
        }

        public ResultHandler extractResultHandler(Object[] args) {
            return hasResultHandler() ? (ResultHandler) args[resultHandlerIndex] : null;
        }

        public String getMapKey() {
            return mapKey;
        }

        public Class<?> getReturnType() {
            return returnType;
        }

        public boolean returnsMany() {
            return returnsMany;
        }

        public boolean returnsMap() {
            return returnsMap;
        }

        public boolean returnsVoid() {
            return returnsVoid;
        }

        public boolean returnsCursor() {
            return returnsCursor;
        }


        private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
            Integer index = null;
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                if (paramType.isAssignableFrom(argTypes[i])) {
                    if (index == null) {
                        index = i;
                    } else {
                        throw new BindingException(method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
                    }
                }
            }
            return index;
        }

        private String getMapKey(Method method) {
            String mapKey = null;
            if (Map.class.isAssignableFrom(method.getReturnType())) {
                final MapKey mapKeyAnnotation = method.getAnnotation(MapKey.class);
                if (mapKeyAnnotation != null) {
                    mapKey = mapKeyAnnotation.value();
                }
            }
            return mapKey;
        }
    }
}
