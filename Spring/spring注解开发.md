# Spring注解驱动开发

### @Configuration & @Bean向容器中注册组件


​	xml配置:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="person" class="com.zgy.annotation.Person">
        <property name="age" value="18"></property>
        <property name="name" value="tom"></property>
    </bean>
</beans>
```

​	测试：

```java
@Test
    public void test(){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("bean.xml");
        Person person = (Person) context.getBean("person");
        System.out.println(person);
    }
```

#### 	使用@Configuration & @Bean

```java
@Configuration
public class Config {
    @Bean("person1")
    public Person person(){
        return new Person("tom", 18);
    }
}
```

```java
@Test
    public void test(){
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        Person person = (Person) context.getBean("person1");
        System.out.println(person);
    }
```

### @ComponentScan

​	@ComponentScan	value : 指定要扫描的包

​	excludeFilters = Filter[]  :  指定扫描的时候按照扫描规则排序那些组件

​	includeFilter = Filter[]  ： 指定扫描的时候只需要包含哪些包

​	FilterType.ANNOTATION : 按照注解

​	FilterType.ASSIGNABLE_TYPE : 按照给定的类型

​	FilterType.ASPECTJ : 使用aspectj规则

​	FilterType.CUSTOM:使用自定义规则

​	FilterType.REGEX : 使用正则规则

```java
@Configuration
@ComponentScan(value = "com.zgy.annotation", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {Controller.class, Service.class})
})
public class Config {
    @Bean("person1")
    public Person person(){
        return new Person("tom", 18);
    }
}
```

### @Scope设置组件作用域

​	prototype: 多实例，获取对象时才会调用方法创建对象。

​	singleton : 单实例（默认值）,ioc容器启动会调用方法创建对象放到ioc容器中

​	request： 同一请求创建一个实例

​	session： 同一个session创建一个实例

### @Lazy：懒加载（延迟加载），获取时创建对象

### @Conditional：按照一定规则进行判断，满足条件给容器中注册bean 

```java
public class WindowsCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        //判断是否是linux系统
        String property = environment.getProperty("os.name");
        if(property.contains("Windows")){
            return true;
        }
        return false;
    }
}
```

```java
public class LinuxCondition implements Condition {
    /**
     *
     * @param conditionContext : 判断条件能使用的上下文环境
     * @param annotatedTypeMetadata：注释信息
     * @return
     */
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

        //能获取ioc使用的beanfactory
        ConfigurableListableBeanFactory beanFactory = conditionContext.getBeanFactory();
        //能过去类加载器
        ClassLoader classLoader = conditionContext.getClassLoader();
        //能获取当前环境信息
        Environment environment = conditionContext.getEnvironment();
        //能获得到bean定义的注册类
        BeanDefinitionRegistry registry = conditionContext.getRegistry();

        //判断是否是linux系统
        String property = environment.getProperty("os.name");
        if(property.contains("linux")){
            return true;
        }
        return false;
    }
}
```

```java
@Configuration
@ComponentScan(value = "com.zgy.annotation", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {Controller.class, Service.class})
})
public class Config {

    /**
     * @Conditional({..}):按照一定规则进行判断，满足要求给容器中注册bean
     * 如果是linux，给容器中注册tom2
     * 如果系统是windows，给容器中注册tom1
     */

    @Conditional(WindowsCondition.class)
    @Bean("person1")
    public Person person1(){
        return new Person("tom1", 18);
    }

    @Conditional(LinuxCondition.class)
    @Bean("person2")
    public Person person2(){
        return new Person("tom2", 18);
    }
}
```

```java

    @Test
    public void test1(){
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        String[] beanNamesForType = context.getBeanNamesForType(Person.class);
        for(String name : beanNamesForType){
            System.out.println(name);
        }
    }
```



### 给容器中注册组件：

​	1.包扫描 + 组件标注注解（@Controller、@Service、@Component、@Repository

​	2.@Bean（导入的第三方包里面的组件）

​	3.@Import 快速给容器中导入一个组件

### @Import

​	@Import（要导入到容器中的组件)，容器就会自动注册这个组件，id默认是全类名。

```java
@Configuration
@ComponentScan(value = "com.zgy.annotation", excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {Controller.class, Service.class})
})
@Import(Color.class)
public class Config{}
```

​	@Import(MyImportSelector (implements ImportSelector)) 

​		ImportSelector:返回需要导入的组件的全类名数组

```java
//自定义逻辑返回需要的组件
public class MyImportSelector implements ImportSelector {

    /**
     * @param annotationMetadata:当前标注@Import注解类的所有注解信息
     * @return
     */
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        return new String[]{"com.zgy.annotation.bean.Blue", "com.zgy.annotation.bean.Yellow"};
    }
}
```

​	@Import(MyImportBeanDefinitionRegistrar (implements ImportBeanDefinitionRegistrar))

​		ImportBeanDefinitionRegistrar：手动注册bean到容器中

```java
public class MyImportBeanDefinitionRegistar implements ImportBeanDefinitionRegistrar {
    /**
     * @param annotationMetadata:当前类的注解信息
     * @param beanDefinitionRegistry：
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        boolean blue = beanDefinitionRegistry.containsBeanDefinition("com.zgy.annotation.bean.Blue");
        boolean yellw = beanDefinitionRegistry.containsBeanDefinition("com.zgy.annotation.bean.Yellow");
        if(blue && yellw){
            RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(Black.class);
            beanDefinitionRegistry.registerBeanDefinition("black!!", rootBeanDefinition);
        }
    }
}
```

​	4.使用Spring提供的FactoryBean（工厂Bean）

​		（1）默认获取到的是工厂Bean调用getObject创建的对象

​		（2）要获取工厂Bean本身，需要在id前面加一个&

```java
public class ColorFactoryBean implements FactoryBean<Color> {
    //返回一个Color对象
    @Override
    public Color getObject() throws Exception {
        return new Color();
    }

    @Override
    public Class<?> getObjectType() {
        return Color.class;
    }

    //是否为单例
    @Override
    public boolean isSingleton() {
        return false;
    }
}
```

```java
@Bean
    public ColorFactoryBean colorFactoryBean(){
        return new ColorFactoryBean();
    }
```

```java
System.out.println(context.getType("&colorFactoryBean"));
//结果 class com.zgy.annotation.bean.ColorFactoryBean
System.out.println(context.getType("colorFactoryBean"));
//结果 class com.zgy.annotation.bean.Color
```

### Bean生命周期

​	（1）构造（对象创建）

​			单实例：在容器启动的时候创建对象

​			多实例：在每次获取的时候创建对象

​	（2）指定初始化方法和销毁方法，

​			通过@Bean指定init-method 和 destroy-method

​				初始化：对象创建完成并赋值好，调用初始化方法

​				销毁：单实例在容器关闭的时候，多实例不会调用销毁方法

```java
@Bean(initMethod = "init", destroyMethod = "destory")
    public Black black(){
        return new Black();
    }
```

​			通过让Bean实现InitializingBean和DisposableBean

```java
public class Cat implements InitializingBean, DisposableBean {

    public Cat(){
        System.out.println("cat constructor...");
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("cat init...");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("cat destroy...");
    }
}
```

​			通过JSR250：

​				@PostConstruct:在bean创建完成并且属性赋值完成时，来执行初始化方法

​				@PreDestroy: 在容器销毁bean之间通知我们进行清理工作

```java
public class Yellow {
    public Yellow(){
        System.out.println("Yellow constructor...");
    }
    @PostConstruct
    public void init(){
        System.out.println("Yellow init...");
    }
    @PreDestroy
    public void destroy(){
        System.out.println("Yellow destroy...");
    }
}
```

​	（3） BeanPostProcessor, bean的后置处理器

​			在bean初始化前后进行一些处理工作（构造器之后）

​			postProcessBeforeInitialization：在初始化之前工作

​			postProcessAfterInitialization：在初始化之后工作

​	***BeanPostProcessor原理***

​		populateBean(beanName, mbd, instanceWrapper):给bean进行属性赋值。

​		initializeBean

​		{

​			applyBeanPostProcessorsBeforeInitialization(bean, beanName)

​			invokeInitMethods(beanName, wrappedBean, mbd):执行初始化

​			applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName)

​		}

​		applyBeanPostProcessorsBeforeInitialization: 遍历得到容器中所有的BeanPostProcessor，挨个执行postProcessBeforeInitialization，一旦返回null，就跳出for循环，不会执行后面的BeanPostProcessor.postProcessorsBeforeInitialization

​	***Spring底层对BeanPostProcessor的使用***

​		bean赋值、注入其他组件、@Autowired、生命周期注解功能等等

### @Value

​	1.基本数值

​	2.SpEL表达式，#{}

​	3.${}，取出配置文件中的值

### @Autowired自动装配

​	（1）默认优先按照类型去容器中找对应的组件

​	（2） 如果找到多个相同类型的组件，再将属性的名称作为组件的id去容器中查找

​	（3）@Qualifier，使用@Qualifier指定需要装配的组件的id，而不是使用属性名

​	（4）自动装配一定要将属性复制，不然找不到组件会报错。也可以使用@Autowired(required=false)

​	（5)@Primary，让Spring进行自动装配的时候，默认使用首先的bean

### java规范的自动装配注解 @Resourse 和 @Inject

​	@Resource：可以和@Autowired一样实现自动装配功能，默认是按照组件名称进行装配，不支持@Primay，不支持required

​	@Inject：需要导入javax.inject的包，和@Autowired功能一样，不支持required

## AOP

### 简单aop示例

​	1.导入aop模块：Spring AOP（spring-aspects）

​	2.定义一个业务逻辑类（MathCalculate）：在业务逻辑运行时将日志进行打印

```java
public class MathCalculate {
    public int div(int a, int b){
        return a / b;
    }
}
```

​	3.定义一个日志切面类（LogAspects）：切面类里面的方法需要动态感知MathCalculate.div运行到哪了

​	通知方法：

​		前置通知：logStart，在目标方法（div）运行之前运行

​		后置通知：logEnd， 在目标方法运行结束之后运行

​		返回通知：logReturn，在目标方法正常返回之后运行

​		异常通知：logException，在目标方法出现异常以后运行

​		环绕通知：动态代理，手动推进目标方法运行（joinPoint.proceed（））

```java
@Aspect
public class LogsAspect {

    @Pointcut("execution(public int com.zgy.aop.MathCalculate.div(..))")
    public void pointCut(){};

    @Before("pointCut()")
    public void logStart(JoinPoint joinPoint){
        System.out.println("before...{"+joinPoint.getSignature().getName()+"}");
    }
    @After("pointCut()")
    public void logEnd(JoinPoint joinPoint){
        System.out.println("after...{"+joinPoint.getSignature().getName()+"}");
    }
    @AfterReturning(value = "pointCut()",returning = "result")
    public void logReturn(Object result){
        System.out.println("正常结束...结果是{"+result+"}");
    }
    @AfterThrowing(value = "pointCut()",throwing = "e")
    public void logException(Exception e){
        System.out.println("异常结束...异常是{"+e+"}");
    }
}
```

​	4.给切面类的目标方法标注何时运行

​	5.将切面类和业务逻辑类（目标方法所在的类）都加入到容器中

​	6.必须告诉spring哪个类是切面类（给切面类上加@Aspect）

​	7.给配置类中加@EnableAspectJAutoProxy（开启基于注解的aop模式）

```java
@Configuration
@EnableAspectJAutoProxy
public class MyConfig {
    @Bean
    public MathCalculate mathCalculate(){
        return new MathCalculate();
    }

    @Bean
    public LogsAspect logsAspect(){
        return new LogsAspect();
    }
}
```

### AOP原理

​	1.@EnableAspectJAutoProxy是什么？

​		给容器注册一个AnnotationAwareAspectJAutoProxyCreator

​	AnnotationAwareAspectJAutoProxyCreator

​	父类->AspectJAwareAdvisorAutoProxyCreator

​	->AbstractAdvisorAutoProxyCreator

​	->AbstractAutoProxyCreator

​		implements 	SmartInstantiationAwareBeanPostProcessor(关注后置处理器，在bean初始化前后做的事情), BeanFactoryAware（自动装配BeanFactoryAware）

​	 AbstractAutoProxyCreator.setBeanFactory()

​	AbstractAutoProxyCreator.有后置处理器的逻辑

​	AbstractAdvisorAutoProxyCreator.setBeanFactory()->initBeanFactory()

​	AnnotationAwareAspectJAutoProxyCreator.initBeanFactory()	 		    

​	**流程：**

​		1.传入配置类，创建ioc容器

​		2.注册配置配，调用refresh()刷新容器(初始化容器)

​		3.registerBeanPostProcessors(beanFactory)：注册bean的后置处理器来方便拦截bean的创建

​			3.1.先获取ioc容器已经定义了的需要创建对象的所有BeanPostProcessor

​			3.2.给容器中加别的BeanPostProcessor

​			3.3.优先注册实现了PriorityOrdered接口的BeanPostProcessor

​			3.4.再给容器中注册实现了Ordered接口的BeanPostProcessor

​			3.5.注册没实现优先级接口的BeanPostProcessor

​			3.6.注册BeanPostProcessor，实际上就是创建BeanPostProcessor对象，保存在容器中

​				创建internalAutoProxyCreator的BeanPostProcessor（AnnotationAwareAspectJAutoProxyCreator）

​				1.创建Bean实例

​				2.poplateBean：给bean属性赋值

​				3.initializeBean：初始化Bean

​					1.invokeAwareMethods(),处理Aware接口的方法回调，其中会走到 AnnotationAwareAspectJAutoProxyCreator.initBeanFactory()	

​					2.applyBeanPostProcessorsBeforeInitialization

​					3.invokeInitMethods()：执行自定义的初始化方法

​					4.applyBeanPostProcessorsAfterInitialization

​				4.BeanPostProcessor（AnnotationAwareAspectJAutoProxyCreator）创建成功

​			3.7.把BeanPostProcessor注册到BeanFactory容器中：

​				beanFactory.addBeanPostProcessor（postProcessor）

​		***以上是创建和注册AnnotationAwareAspectJAutoProxyCreator的过程***

​		***AnnotationAwareAspectJAutoProxyCreator是InstantiationAwareBeanPostProcessor后置处理器***

​		**BeanPostProcessor是在Bean对象创建完成，初始化前后调用的**

​		**InstantiationAwareBeanPostProcessor是在创建Bean实例之前先尝试使用后置处理器返回对象的**

​		**AnnotationAwareAspectJAutoProxyCreator会在任何Bean创建之前先尝试返回bean实例**

​		4.finishBeanFactoryInitialization()：完成BeanFactory初始化工作，创建剩下的单实例Bean

​			1.遍历获取容器中所有的Bean，依次创建对象

​			2.创建Bean

​				1.先从缓存中获取bean，如果能获取到，说明bean在之前已经被创建，直接使用。否则，创建bean

​				2.creatBean()

​					1.resolveBeforeInstantiation(beanName, mbdToUse)：解析BeforeInstantiation，希望后置处理器在此能返回一个代理对象，如果能返回代理对象就使用，如果不能就继续。

​						1.后置处理器先尝试返回对象：

```java
		bean = applyBeanPostProcessorsBeforeInstantiation()//拿到所有后置处理器，如果是InstantiationAwareBeanPostProcessor，就执行postProcessorsBeforeInstantiation方法
		if (bean != null) { 
          bean = this.applyBeanPostProcessorsAfterInitialization(bean, beanName);
         }
```

​					2.doCreatBean()：真正的取创建一个bean实例，和3.6流程一致



​			AnnotationAwareAspectJAutoProxyCreator（InstantiationAwareBeanPostProcessor）的作用：在每一个bean创建之前，调用postProcessBeforeInstantiation（）：

​			关心MathCalculate和LogAspect的创建：

​			1.判断当前bean是否在advisorBeans中（保存了需要增强的bean）

​			2.判断当前bean是否是基础类型的，（Advice、Pointcut、Advisor、AopInfrastructureBean）或者是否是切面（@Aspect）

​			3.是否需要跳过

​				1.获取候选的增强器（切面中通知方法）`List<Advisor> candidateAdvisors`，每一个封装的通知方法的增强器的是InstantiationModelAwarePointcutAdvisor类型

​				2.判断每一个增强器是否是AspectJPointcutAdvisor类型，是返回true

​		创建对象

​		postProcessAfterInitialization():return wrapIfNecessary()//包装如果需要的话

​			1.获取当前bean的所有增强器（通知方法）

​				1.找到候选的所有增强器（找到哪些通知方法是需要切入到当前bean方法的）

​				2.获取能在bean使用的增强器

​				3.给增强器排序

​			2.保存当前bean在advisedBeans中

​			3.如果当前bean需要增强，创建当前bean的代理对象

​				1.获取所有增强器

​				2.保存到proxyFactory

​				3.创建代理对象（JDK动态代理、CGLIB动态代理）

​		给容器中返回当前组件的通过动态代理增强的对象

​		以后容器中获取到的就是这个组件的代理对象，执行目标方法时，代理对象就会执行通知方法的流程

​	**目标方法执行**

​		容器中保存了组件的代理对象（增强后的对象），这个对象里面保存了详细信息（比如增强器、目标对象、...）

​		1.CglibAopProxy.intercept()；拦截目标方法的执行

​		2.根据ProxyFactory对象获取将要执行的目标方法拦截器链；

`List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);`

​			1.List<Object> interceptorList保存所有拦截器（一个默认 + 4个增强器）

​			2.遍历所有的增强器，将其转为Interceptors

​			3.将增强器转换成MethodInterceptor并加入数组中

​				1.如果是MethodInterceptor，直接加入集合中

​				2.如果不是，使用AdvisorAdapter将增强器转换为MethodInterceptor

​				3.转换完成返回MethodInterceptor数组

​		3.如果没有拦截器链，直接执行目标方法

​			拦截器链（每一个通知方法又被包装为方法拦截器，利用MethodInterceptor机制）

​		4.如果有拦截器链，把需要执行的目标对象、目标方法、拦截器链等信息传入创建一个CglibMethodInvocation对象，并调用 Object returnVal = CglibMethodInvocation.proceed()

​		5.拦截器链的触发过程

​			1.如果没有拦截器直接执行目标方法，或者拦截器的索引和拦截器数组大小一样（-1，或者指定到了最后一个拦截器）执行目标方法。

​			2.链式获取每一个拦截器，拦截器执行invoke方法，每一个拦截器等待下一个拦截器执行完成以后再来执行。

​		**拦截器链的机制，保证通知方法与目标方法的执行顺序**

**总结**

​	1.@EnableAspectJAutoProxy  开启AOP功能

​	2.@EnableAspectJAutoProxy 会给容器中注册一个AnnotationAwareAspectJAutoProxyCreator

​	3.AnnotationAwareAspectJAutoProxyCreator是一个后置处理器

​	4.容器的创建流程：

​		1. registerBeanPostProcessors()注册后置处理器：创建AnnotationAwareAspectJAutoProxyCreator

​		2.finishBeanFactoryInitialization初始化剩下的单实例bean

​			1.创建业务逻辑组件和切面组件

​			2.AnnotationAwareAspectJAutoProxyCreator拦截组件的创建过程

​			3.组件创建完之后，判读组件是否需要增强

​				是：切面的通知方法包装成（Advisor），给业务逻辑组件创建一个代理对象

​	5.执行目标方法

​		1.代理对象执行目标方法

​		2.CglibAopProxy.intercept()进行拦截

​			1.得到目标方法的拦截器链（增强器包装成拦截器MethodInterceptor）

​			2.利用拦截器链式机制，依次进入每一个拦截器进行执行

​			3.效果：

​				正常执行：前置通知--》目标方法--》后置通知--》返回通知

​				出现异常：前置通知--》目标方法--》后置通知--》异常通知