## Advanced Java

### Common Sense, or say, basic knowledge

A Java class without an access modifier, it is called default access modifier. The scope of this modifier is limited to this package only. But fields without access modifier will be treated as protected.(Which means users can access them in another class of same package or another subclass of current class).

```java
subString(int beginIndex);//begins with *beginIndex* and extends to the end of the string.
subString(int beginIndex,int endIndex);// begins with *beginIndex* and extends to the *endIndex* but the character at the index of *endIndex* is not included.
```
#### 接口和抽象类的选择:
>子类之间如果没有相同的代码,没有相同的操作,则直接用接口
子类会对共有的属性进行修改和访问,那么接口不满足
抽象类定义的规范更像是is-a,而接口更像like-a
>>狼,羊 is a 动物\
```List,Set,Queue``` like a ```Collection```

>接口和抽象类的区别和联系*略

**2.try catch finally**
return执行前不管有没有捕获到异常，finally中的代码都会执行，但是finally中对不同数据类型的操作结果不同，对值类型的操作不起作用，对引用类型的操作起作用，即会影响到最终结果。

```java
package advanced.java.commonSense.trycatch;

import java.util.Arrays;

public class TryCatchTesterBeta {
    public static void main(String[] args) {
        System.out.println(test());
        System.out.println(Arrays.toString(testQuote()));
    }

    public static String test() {
        String a = "init";

        try {
            System.out.println(a);

            a=a+" append body";
            return a;
        } catch (Exception e) {
            a=a+" append catch";
            return a;
        } finally {
            a=a+" append finally";
            System.out.println("finally "+a);
        }
    }

    public static int[] testQuote(){
        int[] b = new int[]{0};
        try {
            System.out.println(Arrays.toString(b));
            return b;
        } catch (Exception e) {
            e.printStackTrace();
            return b;
        } finally {
            b[0]=3;
            System.out.println(Arrays.toString(b));
        }
    }
}
//上述代码中a不会变，而b会变
```

**3. FunctionalInterface**\
an interface contains only one abstract method is known as functional interface or single abstract method interface, that is, SAM interfaces.
use anonymous class can easily implements SAM interfaces or functional interfaces.\
**Predicate<T> Function<T,R> Comparator<T1,T2>** are 3 commonly used functional interfaces.

```java
@FunctionalInterface
interface Predicate<T>{
    boolean test(T o);
}

@FunctionalInterface
interface Function<T,R>{
    R apply(T t);
}

@FuncionalInterface
interface Comparator<T>{
    // return a negative number,if t1 is less than t2

    int compare(T t1,T t2);
}
```

*special use of method reference*\
when implementing a functional interface in the parameter, and the method in the functional interface keeps its first parameter of the same type as the instance on which the concrete method runs,then you can use ```Class::non-static method``` to realize the SAM interface\
|other common formats|
|:--|
|**instance::non-static method with totally the same parameters with the method in SAM interface**|
|**ClassName::static method with totally the same parameters with the method in SAM interface**|
|上述说法并不完整,使用方法引用时,参数表并不需要完全一致,只需要前面的类名和参数表中的第一个参数是同一个类即可,具体的实现方法可以是单参|


### JVM

#### object

In the header of an object, there are three parts always, they are **object header**,**instance data**, and **padding**.

In the object header, there are mark word, klass pointer. *Besides, object header is also where length field resides if the object is an instance of array.*

```java
import org.openjdk.jol.info.ClassLayout;

class Person {
    private BodyParams bp;
}

public class TestJol {
    public static void main(String[] args) {
        Person person = new Person();
        System.out.println(ClassLayout.parseInstance(person).toPrintable());
    }
}
```

*the running result of codes above will be like:*

![image-jol1](./images/jol1.jpg)

**jvm结构**

类加载器(Class Loader):①把Java代码转换成字节码

运行时数据区(Runtime Data Area):②把字节码加载到内存中，~~命令解析器执行引擎~~将字节码翻译成机器码，该过程需要调用*本地库接口*

执行引擎(Execution Engine):上面的删除线

本地库接口(Native Interface):上面的斜体

#### 运行时数据区

![image-jvm1](./images/javamem1.png)\
*Java虚拟机运行时数据区*



### 线程和线程池

##### create threads

①inherit the ***Thread*** class, and rewrite the *run* method of theThread class

calling run() is equivalent to calling a method in a class and does not create a new thread,but calling start() creates a brand new thread.

*as code list shown below*

```java
//============MyThread.java==============
package advanced.java.threads;

public class MyThread extends Thread{
    public MyThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+"11oo");
    }
}
//===========TestThread.java============
package advanced.java.threads;

public class TestThread {
    public static void main(String[] args) throws InterruptedException {
        MyThread myThread = new MyThread("gzq");
        myThread.start();
        myThread.join();//gzq11oo
        //myThread.run();//main11oo
    }
}
```

②implement the *run* method of the interface ***Runnable***

③Use a thread pool mode :***callable*** interface

### 锁

### 垃圾回收机制

### 中间件

#### memcached

和redis目的一样，通过缓存数据库查询结果，降低访问数据库的次数，提高动态web应用的速度，提高其可拓展性

key-value

![memcached](./images/memcached.jpg)

第一次：RDBMS读取到memcached并同时显示（蓝色）

第二次：直接从memcached读（绿色）

#### mq
**rabbitmq**\
*jargons*:\
```producing```: send messages\
```queue```:producers send messages to queue and consumers try to receive data form queue\
```consuming```:receiving messages

##### 三种消息丢失的原因和对应的解决方法
>1.生产者发送到exchange或者直接发给queue时,因为网络问题消息投递失败
>>mq服务器有confirm模式和returnedMessages()回调函数通知生产者\
>>confirm是来自exchange的确认,而如果routingKey对应的queue没有找到或者找到但丢消息的话,会回调returnedMessages()通知生产者

>2.队列服务器收到了消息,但是没有持久化\
>
>>创建exchange和queue修改对应配置为true(一般叫durable)\
```java
    // SpringAMQP对应的配置
    @Bean
    public DirectExchange messageDirectExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(QueueConstants.MESSAGE_EXCHANGE)
                .durable(true)
                .build();
    }
    @Bean
    public Queue messageQueue() {
        return QueueBuilder.durable(QueueConstants.MESSAGE_QUEUE_NAME)
                .build();
    }
```
>3.消费者程序出错/网络波动等, 导致假的消费
>
>>手动消费,basicNack时,指定将消息放回
```java
    // 使用Nack可能出现的问题
    // 1 刚放回队列的消息马上又被消费,死循环

    // 2 手动ack性能下降

    // 3 double ack,自动手动同时开启,导致手动的返回参数中有0值或者null值

    // 4 和队列的通信延迟,一直是unacked模式,队列重启之后才会进入ready状态,导致队列过大
```

sum: 三个大的方向:**成功投递,成功保存,成功消费**
### todo
### Collections classes in java

#### PriorityQueue

a minimum balanced heap

```java
void grow(int capacity){
    // double if small, else increase by 50%
    if(!capacity){
        int newCapacity = oldCapacity+((oldCapacity<64)?(oldCapacity+2):(oldCapacity>>1));
    }
    queue = Array.copyOf(queue,newCapacity);
}
```
## Tomcat

**不使用SpringMVC的DispatcherServlet怎样将Spring的ApplicationContext加载到Tomcat'容器'中？**

Spring-web包中提供了ContextLoaderListener这个监听器，自定义一个继承ContextLoaderListener的子类，在contextInitialized方法中把ApplicationContext进行保存，例如保存成静态属性。

```java
public class GlobalListener extends ContextLoaderListener{

    private static ApplicationContext ioc;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        //after the initiation,there should be an spring application context in the tomcat
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
        ioc = context;
    }
}
```

### 手写自己的服务器

ServerSocket和Socket两个类

ServerSocket对某个端口进行监听，Socket对该端口发送请求

***程序员可以通过覆盖init方法来写那些仅仅只要运行一次的初始化代码，例如加载数据库驱动，值初始化等等。在其他情况下，init方法通常是留空的***

***destroy 方法提供了一个机会来清理任何已经被占用的资源，例如内存，文件句柄和线程，并确保任何持久化状态和 servlet的内存当前状态是同步的***

![image-tomcat1](./images/tomcat1.png)

*a diy server* **connector and container are combined together**,container mainly process the socket, in HttpServer.java, the processing of request and start server are put together. 

Here comes Facade design pattern. 

In the *service* method of **XXXServlet.java**, it takes 2 parameters from **ServletProcessor.java** which were upcast to ServletRequest and ServletResponse.

Programmers knows the internal workings of the servlet  container can downcast these 2 arguments back and use their *parse* and *sendStaticResource* method respectively.

![image-tomcat2](./images/tomcat2.png) *Connector-processor pattern*

### Bootstrap

bootstrap run the connector with thread

### Connector

connector is responsible for creating a socket waiting the incoming HTTP request, then creating an HttpProcessor for each request and calling its *process()* method.

initialize() calls *open()* in which use **factory design pattern**

maintain a pool of HttpProcessors.

#### StringManager

**StringManager**--Singleton design pattern,make its constructor private and declare a method named getManager.

LocalStrings.properties--which the strings that this packages use reside

*Connetor save CPU cycles by leaving the parameter parsing until it is needed by the servlet.*

#### what does the connector do & what's in the connector of Tomcat

connector use SocketInputStream to read byte streams from the socket's inputstream.

```java
SocketInputStream sis = new SocketInputStream(accept.getInputStream());
sis.readRequestLine();//returns the first line of an http request i.e. POST /servlet/HServlet HTTP/1.1
sis.readHeader();
```

then connector call processor 

```java
HttpProcessor processor = new HttpProcessor(Socket accept);
processor.
```

### Processor

processor is actually a support class for connector. it is assigned to create a Request and Response object for every socket(request),

```java
// process method in HttpProcessor.java
public void process(Socket socket){
    // ...
     this.connector.getContainer().invoke(this.request, this.response);
    // ...
}
```

### Container ( an interface in package *org.apache.catalina.Container*)

four related interfaces of container: Pipeline, Valve, ValveContext and Contained\
and there are 4 types of container in Tomcat4,\
**Engine,Host,Context,Wrapper**\
Enigne represents entire Catalina engine\
Host represents a virtual host with a number of context,like localhost\
Context represents a web application holding several servlets\
Wrapper represents an individual servlet\

### HttpRequest Object

##### reading the inputstream

```java
/**
 * 2 important methods: parse and parseUri
 */
```
but actually, there's a class called SocketInputStream(org.apache.catalina.connector.http.SocketInputStream) which provides methods for following functions.(parsing request line and headers)

```java
//HttpProcessor.java
public class HttpProcessor(){

    // the request that is being handled
    private HttpRequestImpl request = null;

    // the StringManager which is used to get error messages
    protected StringManager sm =
        StringManager.getManager(Constants.Package);

    // the requestLine object which is used to store the request content
    private HttpRequestLine requestLine = new HttpRequestLine();
    ...

    private void parseRequest(SocketInputStream input, OutputStream output)
        throws IOException, ServletException {

        // Parse the incoming request line
        input.readRequestLine(requestLine);
        // set several properties in request

        // checking if the uri is an absolute uri

        // parsing jsessionId
        normalize()
    }
}
```

##### parsing the request line

```java
//HttpRequestLine.java
```

##### parsing the request header

##### parsing cookies

##### obtaining parameters

### HttpResponse Object

### HTTP1.1 new features

![image-http1.1](./images/Http1.1.png)

the "connection: keep-alive" is declared in the request header to indicate explicitly that the browser is using persistent connection while request.

### StandardContextValve

a class in tomcat4 which disallow request resources reside /WEB-INF and /META-INF

```java
// invoke method of StandardContextValve
HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
String contextPath = hreq.getContextPath();
String requestURI = ((HttpRequest) request).getDecodedRequestURI();
String relativeURI = requestURI.substring(contextPath.length()).toUpperCase();
        if (relativeURI.equals("/META-INF") ||
            relativeURI.equals("/WEB-INF") ||
            relativeURI.startsWith("/META-INF/") ||
            relativeURI.startsWith("/WEB-INF/")) {
            notFound(requestURI, (HttpServletResponse) response.getResponse());
            return;
        }
```

### Mapper

When there are several children containers in a parent container like wrappers reside in context, here comes *Mapper*

```java
package org.apache.catalina;
public interface Mapper {
 public Container getContainer();
 public void setContainer(Container container);
 public String getProtocol();
 public void setProtocol(String protocol);
 public Container map(Request request, boolean update);
}
```

the map method return the corrensponding container.In other words, it helps a container to select a child container which will process the request.

### Delegation model of Tomcat
3 aspects using custom class loader\
 [0] specify certain rules in loading classes 指定自定义的类加载规则\ 
 [1] cache loaded classes 将加载过的类缓存下来\
 [2] pre-load classes so they are ready to use 提前加载

5 steps of a **WebappLoader**\
[0] creating a class loader\
[1] set repositories\
[2] set class path\
[3] set permissions\
[4] start a new thread for auto-reload

### websocket
```java
    //websocket不是单例模式,每个新的会话都会新创建一个带有@ServerEndpoint注解的类,所以维护连接的成员变量一定要写成静态的,否则会丢失
/**
 * @author Leonard
 * @date 2022/10/25 16:58
 */
@Component
@ServerEndpoint("/message")
public class MQEndpoint {

    private Logger logger = LogManager.getLogger(MQEndpoint.class);

    private static CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void addClient(Session s){
        logger.info("some one connect the socket");
        logger.error(this);
        sessions.add(s);
    }

    @Override
    public String toString() {
        return getClass().getName()+hashCode();
    }
}
// 上述代码通过每次OnOpen输出HashCode发现,每次的结果都不同.
```
![serverendpoint](./images/ServerEndpointHashCode.png)
## Spring

>四大核心依赖：***beans core context expression*** 

### Spring src

*BeanCurrentlyInCreationException* 当前正在创建池的概念，发现创建过程中已经在这个池中，则抛出该异常，出现在spring bean循环依赖中

```java
//Requested bean is currently in creation, is there an unresolvable circular reference?
```

1. AbstractAutowireCapableBeanFactory入口
   该类负责@AutoWired注解的注入工作
   
   ```java
   doCreateBean(String beanName,RootBeanDefinition mbd,@Nullable Object[] args){
   ...
   instanceWrapper = createBeanInstance(beanName, mbd, args);
   ...
   boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&isSingletonCurrentlyInCreation(beanName));
   // 提前暴露 ObjectFactory
   if (earlySingletonExposure) {
       // 向三级缓存singletonFactories中添加匿名内部类
       addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
   }
   try {
       // 填充属性
       // 在populationBean方法中貌似加入了后置处理器，在真正填充属性之前调用后置处理器，有待考证
       populateBean(beanName, mbd, instanceWrapper);
       // 初始化bean
       exposedObject = initializeBean(beanName, exposedObject, mbd);
   }
   }
   ```
   
   ***接上面的addSingletonFactory***
   
   ```java
   // 三级缓存中存放的是一些ObjectFactory，最终使用到的是其getEarlyBeanReference（）方法
   protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
       Assert.notNull(singletonFactory, "Singleton factory must not be null");
       synchronized (this.singletonObjects) {
           if (!this.singletonObjects.containsKey(beanName)) {
               this.singletonFactories.put(beanName, singletonFactory);
               this.earlySingletonObjects.remove(beanName);
               this.registeredSingletons.add(beanName);
           }
       }
   }
   ```

三级缓存相关的代码位于DefaultSingletonBeanRegistry中 \
三级缓存 singletonFactories HashMap\
二级缓存 earlySingletonObjects ConcurrentHashMap\
如果目标类没有被AOP代理，那么二级缓存可有可无。二级缓存存在意义：被AOP代理之后的对象每调用一次getObject()(ObjectFactory中的方法，最终调用的是上面匿名内部类的getEarlyBeanReference方法，**涉及到lambda表达式实现SAM接口**)就生成一个新代理对象，但是该对象是单例，所以解决办法是从三级缓存中拿出来放到二级缓存中，下次拿这个被AOP代理的对象时直接去earlySingletonObjects中取，而不是新生成一个代理对象，保证了其singleton的属性。\
[博客园：spring三级缓存](https://www.cnblogs.com/semi-sub/p/13548479.html)

### 我碰到的异常

***nested exception is java.lang.IllegalStateException: Ambiguous mapping. Cannot map 'disposeMoldPartController' method***

```java
MappingJackson2HttpMessageConverterConfiguration默认集成到了Spring Boot中，所以Spring Boot结合Spring MVC能把普通java对象转换成JSON数据
```

pom.xml逐行解析，中途出错会导致后续的maven依赖注入失败，

**报红的maven依赖一定要写在最末尾**

处理commons-dbcp 、commons-pool2和其他包之间的依赖问题，这几个包容易出现冲突

jedis-2.2.1是最后一个使用commons-pool的版本，从jedis-2.3.0开始使用commons-pool2

```java
@SuppressWarnings
```

常见的值有：

deprecation:忽略不赞成的警告

```java
//no serializer found for class一般是因为一个类转JSON时没有get set方法导致的，序列化相关
```

### junit

junit5的引擎叫Jupiter,junit4的叫vintage，使用对应的，其余的要在maven中exclude掉  

需要的jar包有

![image-junit5](./images/junitDependencies.jpg)

### vue-cli指令

npm install -g vue-cli安装脚手架，成功之后继续

vue init webpack 项目名称

之后的配置中要选择eslint和run npm install 以及 vue-router

测试相关的可以不选

之后就可以运行dev script启动项目

### 数组

数组分为condensed array 和literal array

### Vue

vue如果遇到v-model不同步更新的话，说明该对象没有该属性，例如input中的值只是在出发onchange和onblur之后才会更新，需要使用this.$set(object,'field you want','value of the field');

setInterval(function,timeout);

setTimeout(function,timeout);

```javascript
//查询结果存到一个数组里,
[
key:aa,
deptName:bb,    
subTree:[
 {
    key:xx,
    deptName:xxx,
    subTree:[
     {

 }
]
 },
 {
     key:yy,
     deptName:yyy,
     subTree:[
         //......
     ]         
 }
]]
```

ERP=enterprise resource planning

CRM=customers relationship management

PDM=product data management

```java
@Autowired
private StudentService service;
/**
* 上述方法是选择依赖注入
* 在使用到StudentService的时候才会进行注入，容易出现
* 空指针异常
*/

/**
* idea推荐使用构造器的方法进行注入
* 在初始化时就进行依赖注入，这样在检查代码时就会进行依赖
* 的校验，为空时就会出现空指针异常，不会在运行时才报错
*/
final StudentService service;

public StudentController(StudentService service){
    this.service = service;
}
```

mybatis 语句经常在 insert 中调用序列，或者类似序列等自动生成某个字段的selectKey，如果当前insert语句中有参数的话，这个selectKey会调用参数中的set方法，之后调用get方法，如果写作

```java
@Param("tom")
//那么在xml文件中读取时应该使用tom
```

```java
String.format("%04d",i);
//这段代码表示将i变成长度为4的字符串，不够4位前面补零
```

### activiti

*historyService查询到的task有是否finished()即是否完成之分*。因为未完成的task也会入ACT_HI_ACTINST表，查询时要注意。

```java
historyService.createHistoricTaskInstanceQuery().finished().....list();
```

查询历史记录时，activiti提供了listpage(index,limit)方法，用来限制条目数量

## spring事务传播机制 propagation

### 分类

| 分类                                          |
| ------------------------------------------- |
| required(Spring默认)                          |
| requires_new(开启新事务，被调用的方法异常会冒泡到调用的方法，父不影响子) |
| never(有事务会报错)                               |
| supports(没事务就依照没事务的情形执行)                    |
| mandatory(没有事务会报错)                          |
| not_supported(有事务直接忽视)                      |
| nested(类比required_new 调用会影响被调用)父子整体         |

## SSM

#### SpringMVC

流程实现 dispatchServlet

#### SpringAOP

AOP的实现

## mysql

### 数据结构

##### 索引
InnoDB的数据结构是**B树和B+树**,\
8.0的文档中: InnoDB的全文索引使用倒排表```inverted lists```\
多列的索引,leftmost可以被重复引用,例如(a,b,c)上的索引可以被用作,(a),(a,b),(a,b,c)三种查询\

索引注意事项:\
where子句中进行比较的两个量\
①类型长度相同,varchar(10)和char(10)可以\ varchar(15)和char(10)不使用索引
②模糊的字符串变量不使用例如比较a.name=b.age时,b.age=1,a.name=' 1',判定成立但是不走索引\
③
###### B树


### 隔离级别

8.x之前的版本使用@@tx_isolation

8.x及8.x之后使用transaction_isolation,@@tx_isolation会报错找不到,而且从5开始就已经要弃用tx_isolation了

![image-20220425102335476](./images/mysqlison.png)
![image-mysqlwarning](./images/mysqlwarning.png)

set session transaction isolation level + following 4 choices

```sql
read committed
read uncommitted
repeatable read
serializable
```

default repeatable-read, which can run without the risk of dirty read and non-repeatable read  or lost read,but can not solve phantom read

```java
/**
 * don't use more than 5 **join**
 * take temporary tables into account
 * fewer sub-retrieval
 * view nests should be no more than 2 layers
 */
```

### 前后端数据格式之间的对应关系

| 前端              | 后端接收方式                                           |
| --------------- | ------------------------------------------------ |
| Request PayLoad | 整个request之后进行解析,                                 |
| 普通键值对，form data | 无需注解，POJO只要有对应属性的set方法即可赋值,或者通过@RequestParam进行映射 |
| 只传数组            | @RequestBody List<T>即可                           |

***注意事项*** 通过api中的request发请求携带的参数一般是request payload(POST) ,而其他一般是form data例如this.download等方法

前者需要通过键值对进行POJO封装和@RequestBody进行结合使用，后者直接@RequestParam

##### TIP1

如果封装的属性或者参数个数很少，或者项目中的视图类viewObject太多，可以在前端进行手动封装

```javascript
//数组对象People=[person1,person2,person3]
//字符串remark="用户操作000"

var obj = new FormData();
obj.append("people",JSON.stringify(People));
obj.append("remark",remark);

axios({
    method:'post',
    data:obj,
    ……
})
```

```java
import com.byd.gzq.Person;

@PostMapping("/上面请求的url")
public String personOperation(@RequestParam("people") String people,@RequestParam("remark") String remark){
    List<Person> personList = JSON.parseArray(people,Person.class);
    ……
}
```

InternalResourceViewResolver视图解析器继承了UrlBasedViewResolver，两个属性叫做prefix和suffix

如果不指定springmvc配置文件的位置，默认去找前端控制器名-servlet.xml

Tomcat服务器有一个大的web.xml配置文件，所有项目的web.xml文件都是继承自这个大的web.xml

在大的web.xml中，url-pattern始终是/，即不拦截*.jsp请求

```xml
<servlet>
  <servlet-name>default</servlet-name>
  <servlet-class>org.apache.catalina.servlets.DefaultServlet</servlet-class>
    <init-param>
      <param-name>debug</param-name>
      <param-value>0</param-value>
    </init-param>
    <init-param>
      <param-name>listings</param-name>
      <param-value>false</param-value>
    </init-param>
  <load-on-startup>1</load-on-startup>
</servlet>

<servlet-mapping>
   <servlet-name>default</servlet-name>
   <url-pattern>/</url-pattern>
</servlet-mapping>
<!--摘自Tomcat conf/web.xml-->
```

oracle进行模糊查询时，若包含通配符，使用instr会提高效率，而弃用like '%xxx%'

枚举类会在初始化时按照成员的声明顺序依次调用构造方法，不管实际引用了或者使用了哪些成员

```java
public enum Color {
    RED{
        @Override
        public String getInfo(){
            return "red";
        }
    },BLUE{
        @Override
        public String getInfo() {
            return "blue";
        }
    },BLACK{
        @Override
        public String getInfo() {
            return "black";
        }
    };
    private Color(){
        System.out.println(this.name());
    }

    public abstract String getInfo();
}
//================================
public class EnumApplication {
    public static void main(String[] args) {
        Color a = Color.BLACK;
        System.out.println(a.getInfo());
    }
}
//上述代码的结果将是
RED
BLUE
BLACK
black
//可以利用这种机制进行输出每个元素，《数据结构》
在Dictionary中就有返回元素枚举值的方法,elements
```

比较时把数字写在前面可以避免NullPointerException

```java
if (1 == masPdmApiMapper.addProjectData(pdmProjectTmp)) {
    lStoreProjectId = pdmProjectTmp.getId();
}
```

#### 数据恢复

```sql
#使用该sql查询对某个表的
SELECT r.first_load_time,r.* FROM v$sqlarea r ORDER BY r.FIRST_LOAD_TIME DESC;
    SELECT * FROM D_S_MATERIEL AS OF timestamp to_timestamp('2022-03-05 09:21:38','yyyy-mm-dd hh24:mi:ss');
ALTER TABLE D_S_MATERIEL enable ROW movement;
flashback TABLE D_S_MATERIEL TO timestamp to_timestamp('2022-03-05 09:21:38','yyyy-mm-dd hh24:mi:ss');
```

### suggested commit message

```markdown
<type>(scope):_description(there's a blank space between the colon and description)

feat(mapper): description
fix(scope): description
refactor(scope): description
docs:only
perf
revert
```

## 日志框架

### Log4j

log4j有三个主要的组件：

loggers记录器，appenders输出源，layouts布局

#### loggers

#### appenders

常用的Appender：

```java
org.apache.log4j.ConsoleAppender;
org.apache.log4j.FileAppender;
org.apache.log4j.DailyRollingFileAppender;
org.apache.log4j.RollingFileAppender;
org.apache.log4j.WriterAppender;
```

在properties格式的配置文件中应该写成：

```properties
log4j.appender.[appenderName] = [上面的全类名]
log4j.appender.[appenderName].属性1 = []
#带有[]的表示该处要用户自己替换
```

配置文件第一句：

```properties
log4j.rootLogger = [level],appenderName1,appenderName2,appenderName3......
#备选level就是info,warn,debug,error,fatal,off,all这些   优先级顺序debug<info<warn<error<fatal
```

着重：PatternLayout.ConversionPattern的备选项

```properties
# %p:priority 代表debug warn这些优先级，
# %t:name of thread
# %r:从程序启动到输出日志所花费的毫秒数
# %c:日志源名 ----loggername
# %C:qualified class name  
# %M:method name of the logger   ----main或者myfunction....
# %F:filename
# %L:line number
# %l:相当于%C.%M(%F:%L)  输出类似 com.byd.gzq.TestClass.main(TestClass.java:22)这种的，全类名加方法名加文件名加行号 全称location,日志发生的位置，匿名类可能是类似com.xxxx.Student$1.methodName这样的
# %m:message
# %n:\n
# 所有的%和标识符之间可以加上限制位数，例如%5p标识优先级占五位
# 注意几对大小写字符的存在,l和L，c和C 
```
### logback
```properties
# logback是log4j的精简和优化,其PatternLayout支持的颜色:
#"%black", "%red", "%green","%yellow","%blue", "%magenta","%cyan", "%white", "%gray", "%boldRed","%boldGreen", "%boldYellow", "%boldBlue", "%boldMagenta","%boldCyan", "%boldWhite" and "%highlight" 
```
## morse code

A 13

B 3111

C 3131

D 311

E 1

## Shell

```shell
#0
#0.1 文件开始最好注明下面任意一种
#!/bin/bash 
#!/bin/sh
#1 变量中除了下划线不能出现别的标点符号
#2 首字符不是数字
#3 声明变量时不用$符号，必须紧挨=
#4 使用变量时${变量名},大括号可省，拼串时定义边界
#5 重复声明不带$
name="helloworld"
echo $name
name="new helloworld"
echo $name

#6 只读变量
r_name="readonly"
readonly r_name
r_name="change readonly"
#输出 r_name:This variable is read only

#7 删除变量unset 对只读变量无效
name="init"
echo $name #init
unset name
echo $name #空

#8 单双引号的区别
##8.1 单引号中的变量无效，所有内容原样输出，转义符没用，单个单引号不能识别，成对出现可以拼串
name='hello''world'++
##8.1 字符串定义时可以不用使用任何引号，上述例子输出 helloworld++

#9 字符串操作
##9.1 长度
name="helloworld"
echo ${#name} #10

##9.2 子串
name="helloworld"
echo #{name:0:3} #hel 从0开始取3个（包括第0个，基标始于0）

##9.3 查下标
name="helloworld"
echo `expr index "$name" io` #i和o谁先出现就返回谁的下标，这里是5，真实位置，下标+1

#10 数组
arr=(char 43 ji)
${arr[0]} #char
$arr #char 不写下标就是第一个
${arr[@]} # char 43 ji 索引位置标@取所有元素

${#arr[0]} #4 想获取某个元素的长度就和之前的获取变量长度一样，先取元素，再操作
${#arr[@]} ${#arr[*]} #3 获取数组的元素个数，使用@或者*

#11 多行注释
:<<EOF
内容。。。
<必须空一行>
EOF

#12 传参
[root@localhost ~]# ./shell.sh a1 a2 a3
#shell.sh的内容
#!/bin/bash
echo $0
echo $1
echo $2
echo $3
#输出 ./shell.sh ##注意$0是命令本身./shell.sh
#a1
#a2
#a3

##12.1 循环
for i in "$@";do  #把参数分开，整体作为数组1 2 3输出1\n2\n3\n
  echo $i
done
for i in "$*";do #把参数作为一个整体，"1 2 3"
  echo $i
done

#13 shell运算
##13.1 shell不支持原生的运算，但是可以通过expr来实现
echo `expr 1 + 3`  #4 注意使用反引号`，运算操作数和操作符不能紧挨，1+3无效，1 + 3正确

##13.2 判断
if[$a == $b]
then
  echo $a
fi

##13.3 ``中的内容要执行一次，若存在错误则无法正确返回 乘法运算符*必须转义\*
```

| -eq | -nq | -gt | -lt | -ge | -le | -o    | -a  | !   |
| --- | --- | --- | --- | --- | --- | ----- | --- | --- |
| ==  | !=  | >   | <   | >=  | <=  | \|\|或 | &&与 | !非  |

```shell
#(续)
#14 sed
    sed -i.suffix 's/old/new/g' filename
#s means find and replace, g means matching globally rather only first occurrence.And suffix behind -i means you wanna make backups when you use -i to update the initial file content.
    sed '2 a something you wanna insert' filename
    sed '/matchstr/i something you wanna insert' filename
    sed '/matchstr/d' filename
#d means delete and i means insert, a is used to insert something based on line number rather than the matching characters.
```

## windows bat
```bat
@rem delete files recursively in a parent directory
for /R %%i in (wanted files) do (
    del %%i
)


@rem change the title of plain cmd, use:
title newtitle
@rem command above run without quotations

@rem change the title of powershell, use:
$host.ui.rawui.windowtitle='newtitle'
@rem command above run with quotations,but when you connect to linux via ssh,the settings will fail which will be changed to sth like root@localhost:~
```



## Redis
### configuration
ip addresses you texted behind ```bind``` should *be accessible* because redis server will try to establish tcp connection via the ip you texted in the .conf file
### redis应该提供基础数据而不是校验结果

例如：把smembers的结果交给程序，而不是sismember的判断结果

**redis set应用场景①**使用set统计网站UV和PV

### RDB

save指令

### cluster
when creating cluster crossing different platforms, mind that you should make the cluster bus port public or just accessible to all the nodes.

## Docker

![image-docker](./images/docker.jpg)

## 系统设计

### 统一返回参数

### 统一参数校验

### 分布式
>how to keep data consistency between redis and mysql 
>>use trigger and ```udf()```\
>>...other ways
#### CAP
```shell
# c=consistency

# a=availability

# p=partition tolerance
# 保证一致性,就会执行锁定的操作,锁定期间无法保证可用;反过来,保证了一直可用,那么数据的一致性就难以保证,所以只能CP或者AP,zookeeper是CP,eureka是AP,eureka更适合做服务注册发现中心,因为返回几秒钟之前的服务比直接服务不可用更容易接受.而AP一般都会选择最终一致性,比如2小时提现到账,一天内退款等等.
```


#### 分布式锁
```txt
使用zookeeper的ephemeral_sequential nodes实现分布式锁
```
```java
create(PATH)
```

### jmeter load test tricks
```shell
# jmeter常用的四个全局变量,可以附在请求名之后
# ${__threadNum}：统计线程数

# ${__counter(true,i)}：只统计该用户的迭代次数，

# ${__counter(false,i)}：统计所有用户的迭代次数，

# ${__threadNum}_${__counter(true,i): 统计每个线程下，每个用户的迭代次数。

```

# C/C++

## 前期配置

gcc版本 Red Hat 4.8.5-4) 

## 基本注意点

### 构造函数

### 析构函数

### 结构体

### 联合体

## 指针

### 基本使用
```C++
#include <iostream>
// 将数组作为入参时,它会退化为指针,此时使用sizeof取长度获取到的是对应类型的指针长度,所以在设计的时候要提前把size作为入参
//"sizeof" on array function parameter will return size of the pointer
```

### 高级使用

## 数据结构

### C实现二叉树

### C实现链表

### 搜索算法

### 排序算法

## C++ STL

# JavaScript

## 模块

只要把不同的函数和记录状态的变量放在一起，就算是一个模块，实现特定功能的一组方法。

### CommonJS

```js
/*there's a file named math.js*/

var math = require('math')
math.funcA(args)
```

commonJS存在的问题：funcA的执行必须等待math.js加载完成，对于服务器端来说，所有模块都在硬盘里，等待时间就是读取硬盘时间，但是在浏览器中情况不同，等待时间取决于网速，此时，AMD诞生了

### AMD(asynchronous module definition)异步模块定义

模块的加载不影响它后面语句的执行,使用回调函数的形式

```js
define(id?,dependencies?:array,factory)
```

**使用原型方法和在构造方法中使用this.xxx有什么区别:**

使用原型定义方法更容易修改，实例化之后也仍然可以通过类名.prototype进行修改，而使用构造方法中的this.xxx定义之后，只能通过对象名依次修改方法体；此外prototype更快更省空间，不会出现每个对象复制一份方法的情况

### js String对象

replace(/[]/g,'') 正则修饰符，后面的```g```是匹配选项，共有```i,g,m```三个选项，i表示忽视大小写，g表示匹配全局而不是匹配到一个就停下，m表示匹配多行



## tricks
```JavaScript
//使用浏览器发送请求可以直接在控制台使用fetch方法
fetch("http://localhost:8080/sfc/componentsTree?level=0")
.then(resp=>{resp.body.getReader().read()
    .then(resps=>{console.log(JSON.parse(String.fromCharCode.apply(null,resps.value)))})});

// 使用fetch读到的是ReadableStream,之后取其body.getReader().read(),将返回的Promise转换为Uint字节数组(Uint8Array),然后使用String.fromCharCode方法转字符串,之后就可以使用自定义的方法进行处理,例如JSON.parse等
```