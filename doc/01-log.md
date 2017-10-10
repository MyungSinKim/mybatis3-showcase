## MyBatis中的日志模块实现分析
#### java项目中常用的日志库有哪些呢?
    1. Log4j(https://logging.apache.org/log4j/1.x/)
    2. Log4j2(https://logging.apache.org/log4j/2.x/)
    3. Apache Commons Log(http://commons.apache.org/proper/commons-logging)
    4. java.util.loging (JDK1.4后内置日志实现)
    5. slf4j(https://www.slf4j.org/index.html)
    6. logback(https://logback.qos.ch/)
    
#### 众多日志实现会造成什么问题?
    每个日志实现的接口是不同的,设置定义的日志级别都不一样.
    
#### 推荐博客
    http://feihu.me/blog/2014/insight-into-log/
    
#### MyBatis面对上面众多的日志实现是处理的?
    Mybatis定义了一套自己的日志接口,MyBatis源代码中不去依赖具体的实现.
    通过适配器模式对各种日志实现进行适配.

#### 设计模式的六大原则?(为后续讲解适配器模式作为铺垫)
    1. 单一职责
    2. 里氏替换
    3. 依赖倒置
    4. 接口隔离
    5. 迪米特法则
    6. 开闭原则(开闭原则是最基础的原则, 也是其他原则的终极目标)

#### 为什么要使用适配器模式?
    《JAVA与模式》这本书中是这样对适配器模式进行描述的 
    "适配器模式把一个类的接口变换成客户端所期待的另一种接口，从而使原本因接口不匹配而无法在一起工作的两个类能够在一起工作。"
    
    知道了MyBatis中面对众多日志实现需要统一给上层模块调用的需求,再联系适配器能够解决的问题.就能够理解MyBatis中为什么要使用
    适配器模式来设计其日志模块了.
    
    使用适配器模式复用现有的组件(也就是具体日志实现),针对每一个具体的日志实现创建一个适配器,这些适配器都实现统一的接口(Log接口).
    这样如果再次增加新的日志实现就可以不修改原有的代码,只要新增适配器.这是一种开闭原则的体现.

#### MyBatis 中提供了哪些日志实现适配器?
    StdOutImpl
    Slf4jImpl
    Log4j2Impl
    Log4j2LoggerImpl
    Log4jImpl
    Log4j2AbstractLoggerImpl 
    NoLoggingImpl 
    Jdk14LoggingImpl 
    Slf4jLoggerImpl 
    JakartaCommonsLoggingImpl 
    Slf4jLocationAwareLoggerImpl 

#### MyBatis 日志模块的初始化入口在哪里?
    LogFactory类中定义了一个静态代码块    

#### MyBatis 日志模块尝试加载日志实现的过程是怎样的?
    按照下面排列的顺序尝试加载(获取构造方法)适配器.
    Slf4j(具体实现log4j)
    CommonsLogging
    Log4j
    Log4j2
    Jdk14Logging
    NoLogging
    
    
    
    
    
    
    
    
    
    
    
    