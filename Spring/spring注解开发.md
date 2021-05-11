# Spring注解驱动开发

### @Configuration & @Bean向容器中注册组件

#### 使用xml配置:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 								http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="person" class="com.zgy.annotation.Person">
        <property name="age" value="18"></property>
        <property name="name" value="tom"></property>
    </bean>
</beans>
```

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
        AnnotationConfigApplicationContext context = new 		               AnnotationConfigApplicationContext(Config.class);
        Person person = (Person) context.getBean("person1");
        System.out.println(person);
    }
```

### @ComponentScan
   &emsp;@ComponentScan	value : 指定要扫描的包

​	&emsp;excludeFilters = Filter[]  :  指定扫描的时候按照扫描规则排除哪些组件

​	&emsp;includeFilter = Filter[]  ： 指定扫描的时候只需要包含哪些包

​	&emsp;FilterType.ANNOTATION : 按照注解

​	&emsp;FilterType.ASSIGNABLE_TYPE : 按照给定的类型

​	&emsp;FilterType.ASPECTJ : 使用aspectj规则

​	&emsp;FilterType.CUSTOM:使用自定义规则

​	&emsp;FilterType.REGEX : 使用正则规则

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

​	&emsp;prototype: 多实例，获取对象时才会调用方法创建对象。

​	&emsp;singleton : 单实例（默认值）,ioc容器启动时会调用方法创建对象并放到ioc容器中

​	&emsp;request： 同一请求创建一个实例

​	&emsp;session： 同一个session创建一个实例

### @Lazy：懒加载（延迟加载），获取时创建对象

### @Conditional：按照一定规则进行判断，满足条件给容器中注册bean 

```java
public class WindowsCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        //判断是否是linux系统
      	Environment environment = conditionContext.getEnvironment();
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
        //能获取类加载器
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

​	1.包扫描 + 组件标注注解（@Controller、@Service、@Component、@Repository）

​	2.@Bean（导入第三方包里面的组件）

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
     * @return 返回需要的组件
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
          	//如果有Blue && Yellow注册一个Black
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
public class Black {
    public Black(){
        System.out.println("black creat...");
    }

    public void init(){
        System.out.println("black init...");
    }

    public void destory(){
        System.out.println("black destory...");
    }
}
```

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

​				@PreDestroy: 在容器销毁bean之前通知我们进行清理工作

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

```java
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
        System.out.println("postProcessBeforeInitialization..." + bean + "-->" + name);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String name) throws BeansException 	{
        System.out.println("postProcessAfterInitialization..." + bean + "-->" + name);
        return bean;
    }
}
```

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

​	（4）自动装配一定要将属性赋值，不然找不到组件会报错。也可以使用@Autowired(required=false)

​	（5)   @Primary，让Spring进行自动装配的时候，默认使用优先的bean

### java规范的自动装配注解 @Resourse 和 @Inject

​	&emsp;@Resource：可以和@Autowired一样实现自动装配功能，默认是按照组件名称进行装配，不支持    @Primay，不支持required

​	&emsp;@Inject：需要导入javax.inject的包，和@Autowired功能一样，不支持required

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
	//提取公共部分
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

​		@EnableAspectJAutoProxy会给容器注册一个AnnotationAwareAspectJAutoProxyCreator

```markdown
	AnnotationAwareAspectJAutoProxyCreator

	父类->AspectJAwareAdvisorAutoProxyCreator

	父类->AbstractAdvisorAutoProxyCreator

	父类->AbstractAutoProxyCreator

		AbstractAutoProxyCreator implements SmartInstantiationAwareBeanPostProcessor(关注后置处理器，在bean初始化前后做的事情), BeanFactoryAware（自动装配BeanFactoryAware）
		
	//给有BeanFactory、BeanPostProcessor逻辑的方法打上端点进行调试
	AbstractAutoProxyCreator.setBeanFactory()

	AbstractAutoProxyCreator.有后置处理器的逻辑

	AbstractAdvisorAutoProxyCreator.setBeanFactory()->initBeanFactory()

	AnnotationAwareAspectJAutoProxyCreator.initBeanFactory()

```

​	**流程：**

​		1.传入配置类，创建ioc容器

​		2.注册配置类，调用refresh()刷新容器(初始化容器)

```java
	public AnnotationConfigApplicationContext(Class<?>... annotatedClasses) {
        this();
        this.register(annotatedClasses);
        this.refresh();
    }
```

​		3.registerBeanPostProcessors()：注册bean的后置处理器来方便拦截bean的创建

```
	  this.registerBeanPostProcessors(beanFactory);
```

​			&emsp;3.1.先获取ioc容器已经定义了的需要创建对象的所有BeanPostProcessor

```java
        String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);
        List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList();
        List<String> orderedPostProcessorNames = new ArrayList();
        List<String> nonOrderedPostProcessorNames = new ArrayList();
		...
        sortPostProcessors(orderedPostProcessors, beanFactory);
        registerBeanPostProcessors(beanFactory, (List)orderedPostProcessors);
		...
        sortPostProcessors(orderedPostProcessors, beanFactory);
        registerBeanPostProcessors(beanFactory, (List)orderedPostProcessors);
		...
        registerBeanPostProcessors(beanFactory, (List)nonOrderedPostProcessors);
        sortPostProcessors(internalPostProcessors, beanFactory);

```

​			&emsp;3.2.给容器中加别的BeanPostProcessor

​			&emsp;3.3.优先注册实现了PriorityOrdered接口的BeanPostProcessor

​			&emsp;3.4.再给容器中注册实现了Ordered接口的BeanPostProcessor

​			&emsp;3.5.注册没实现优先级接口的BeanPostProcessor

​			&emsp;3.6.注册BeanPostProcessor，实际上就是创建BeanPostProcessor对象，保存在容器中

​				&emsp;&emsp;创建internalAutoProxyCreator的BeanPostProcessor(AnnotationAwareAspectJAutoProxyCreator)过程：（例子）

​				&emsp;&emsp;1.创建Bean实例

​				&emsp;&emsp;2.poplateBean：给bean属性赋值

​				&emsp;&emsp;3.initializeBean：初始化Bean

​					&emsp;&emsp;&emsp;1.invokeAwareMethods(),处理Aware接口的方法回调，其中会走到 AnnotationAwareAspectJAutoProxyCreator.initBeanFactory()	

​					&emsp;&emsp;&emsp;2.applyBeanPostProcessorsBeforeInitialization

​					&emsp;&emsp;&emsp;3.invokeInitMethods()：执行自定义的初始化方法

​					&emsp;&emsp;&emsp;4.applyBeanPostProcessorsAfterInitialization

​				&emsp;&emsp;4.BeanPostProcessor（AnnotationAwareAspectJAutoProxyCreator）创建成功

​			&emsp;3.7.把BeanPostProcessor注册到BeanFactory容器中：

​				&emsp;beanFactory.addBeanPostProcessor（postProcessor）

​		&emsp;**以上是创建和注册AnnotationAwareAspectJAutoProxyCreator的过程**

​		&emsp;***AnnotationAwareAspectJAutoProxyCreator是InstantiationAwareBeanPostProcessor后置处理器***

​		&emsp;**BeanPostProcessor是在Bean对象创建完成，初始化前后调用的**

​		&emsp;**InstantiationAwareBeanPostProcessor是在创建Bean实例之前先尝试使用后置处理器返回对象的**

​		&emsp;**AnnotationAwareAspectJAutoProxyCreator会在任何Bean创建之前先尝试返回bean实例**

​		4.finishBeanFactoryInitialization()：创建剩下的单实例Bean，完成BeanFactory初始化工作

```java
	  this.finishBeanFactoryInitialization(beanFactory);
```

​			&emsp;1.遍历获取容器中所有的Bean，依次创建对象

​			&emsp;2.创建Bean

​				&emsp;&emsp;1.先从缓存中获取bean，如果能获取到，说明bean在之前已经被创建，直接使用。否则，创建bean，创建好的bean会缓存起来。

​				&emsp;&emsp;2.creatBean()

​					&emsp;&emsp;&emsp;1.resolveBeforeInstantiation(beanName, mbdToUse)：解析BeforeInstantiation，希望后置处理器在此能返回一个代理对象，如果能返回代理对象就使用，如果不能就继续创建。

```java
		   beanInstance = this.resolveBeforeInstantiation(beanName, mbdToUse);
            if (beanInstance != null) {
                return beanInstance;
            }
			...
            beanInstance = this.doCreateBean(beanName, mbdToUse, args);
```

​						&emsp;&emsp;&emsp;&emsp;1.后置处理器先尝试返回对象：

```java
		bean = applyBeanPostProcessorsBeforeInstantiation()//拿到所有后置处理器，如果是InstantiationAwareBeanPostProcessor，就执行postProcessorsBeforeInstantiation方法
		if (bean != null) { 
          bean = this.applyBeanPostProcessorsAfterInitialization(bean, beanName);
         }
```

​						&emsp;&emsp;&emsp;&emsp;2.doCreatBean()：真正的取创建一个bean实例，和3.6流程一致

​			AnnotationAwareAspectJAutoProxyCreator（InstantiationAwareBeanPostProcessor）的作用：在每一个bean创建之前，调用postProcessBeforeInstantiation（）

​			关心MathCalculate和LogAspect的创建：

​			1.判断当前bean是否在advisorBeans中（保存了需要增强的bean）

​			2.判断当前bean是否是基础类型的，（Advice、Pointcut、Advisor、AopInfrastructureBean）或者是否是切面（@Aspect）

​			3.是否需要跳过

​				&emsp;1.获取候选的增强器（切面中通知方法）`List<Advisor> candidateAdvisors`，每一个封装的通知方法的增强器的是InstantiationModelAwarePointcutAdvisor类型

​				&emsp;2.判断每一个增强器是否是AspectJPointcutAdvisor类型，是返回true

```java
		if (beanName == null || !this.targetSourcedBeans.contains(beanName)) {
            if (this.advisedBeans.containsKey(cacheKey)) {
                return null;
            }

            if (this.isInfrastructureClass(beanClass) || this.shouldSkip(beanClass, beanName)) {
                this.advisedBeans.put(cacheKey, Boolean.FALSE);
                return null;
            }
        }
```

​		4.创建对象

​		postProcessAfterInitialization():return wrapIfNecessary()//包装如果需要的话

​			1.获取当前bean的所有增强器（通知方法）

```
Object[] specificInterceptors = this.getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, (TargetSource)null);
```

​				&emsp;1.找到候选的所有增强器（找到哪些通知方法是需要切入到当前bean方法的）

​				`List<Advisor> candidateAdvisors = this.findCandidateAdvisors();`

​				&emsp;2.获取能在当前bean使用的增强器

`List<Advisor> eligibleAdvisors = this.findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);`

​				&emsp;3.给增强器排序

​				`eligibleAdvisors = this.sortAdvisors(eligibleAdvisors);`

​			2.保存当前bean在advisedBeans中

​				`this.advisedBeans.put(cacheKey, Boolean.TRUE);`

​			3.如果当前bean需要增强，创建当前bean的代理对象

`Object proxy = this.createProxy(bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));`

​				&emsp;1.获取所有增强器

`Advisor[] advisors = this.buildAdvisors(beanName, specificInterceptors);`

​				&emsp;2.保存到proxyFactory

​				`proxyFactory.addAdvisors(advisors);`

​				&emsp;3.创建代理对象（JDK动态代理、CGLIB动态代理）

```java
public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
        if (!config.isOptimize() && !config.isProxyTargetClass() && !this.hasNoUserSuppliedProxyInterfaces(config)) {
            return new JdkDynamicAopProxy(config);
        }
  		else {
            Class<?> targetClass = config.getTargetClass();
            if (targetClass == null) {
                throw new AopConfigException("TargetSource cannot determine target class: Either an interface or a target is required for proxy creation.");
            }
          	else {
                return (AopProxy)(!targetClass.isInterface() && !Proxy.isProxyClass(targetClass) ? new ObjenesisCglibAopProxy(config) : new JdkDynamicAopProxy(config));
            }
        }
}
```

​		给容器中返回当前组件的通过动态代理增强的对象

​		以后容器中获取到的就是这个组件的代理对象，执行目标方法时，代理对象就会执行通知方法的流程

​	**目标方法执行**

​		容器中保存了组件的代理对象（增强后的对象），这个对象里面保存了详细信息（比如增强器、目标对象、...）

​		1.CglibAopProxy.intercept()；拦截目标方法的执行

​		2.根据ProxyFactory对象获取将要执行的目标方法拦截器链；

`List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);`

​			&emsp;1.List<Object> interceptorList保存所有拦截器（一个默认 + 4个增强器）

​			&emsp;2.遍历所有的增强器，将其转为Interceptors

​			&emsp;3.将增强器转换成MethodInterceptor并加入数组中

​				&emsp;&emsp;1.如果是MethodInterceptor，直接加入集合中

​				&emsp;&emsp;2.如果不是，使用AdvisorAdapter将增强器转换为MethodInterceptor

​				&emsp;&emsp;3.转换完成返回MethodInterceptor数组

​		3.如果没有拦截器链，直接执行目标方法

​			拦截器链（每一个通知方法又被包装为方法拦截器，利用MethodInterceptor机制）

```java
if (chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {
     Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
     retVal = methodProxy.invoke(target, argsToUse);
}
```

​		4.如果有拦截器链，把需要执行的目标对象、目标方法、拦截器链等信息传入创建一个CglibMethodInvocation对象，并调用 Object returnVal = CglibMethodInvocation.proceed()

​		5.拦截器链的触发过程

​			&emsp;1.如果没有拦截器直接执行目标方法，或者拦截器的索引和拦截器数组大小一样（-1，或者指定到了最后一个拦截器）执行目标方法。

​			&emsp;2.链式获取每一个拦截器，拦截器执行invoke方法，每一个拦截器等待下一个拦截器执行完成以后再来执行。

```java
public Object proceed() throws Throwable {
     if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            return this.invokeJoinpoint();
     } else {
            Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
           if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
                InterceptorAndDynamicMethodMatcher dm = (InterceptorAndDynamicMethodMatcher)interceptorOrInterceptionAdvice;
                return dm.methodMatcher.matches(this.method, this.targetClass, this.arguments) ? dm.interceptor.invoke(this) : this.proceed();
           } else {
             //invoke中又调用proceed方法，递归调用。所以之前获取增强方法要先排序，将加@before
             //注解的方法放在最后，即最深。
                return ((MethodInterceptor)interceptorOrInterceptionAdvice).invoke(this);
           }
    }
}
```



​		**拦截器链的机制，保证通知方法与目标方法的执行顺序**

**总结**

​	1.@EnableAspectJAutoProxy  开启AOP功能

​	2.@EnableAspectJAutoProxy 会给容器中注册一个AnnotationAwareAspectJAutoProxyCreator

​	3.AnnotationAwareAspectJAutoProxyCreator是一个后置处理器

​	4.容器的创建流程：

​		&emsp;1. registerBeanPostProcessors()注册后置处理器：创建AnnotationAwareAspectJAutoProxyCreator

​		&emsp;2.finishBeanFactoryInitialization初始化剩下的单实例bean

​			&emsp;&emsp;1.创建业务逻辑组件和切面组件

​			&emsp;&emsp;2.AnnotationAwareAspectJAutoProxyCreator拦截组件的创建过程

​			&emsp;&emsp;3.组件创建完之后，判读组件是否需要增强

​				&emsp;&emsp;&emsp;是：切面的通知方法包装成（Advisor），给业务逻辑组件创建一个代理对象

​	5.执行目标方法

​		&emsp;1.代理对象执行目标方法

​		&emsp;2.CglibAopProxy.intercept()进行拦截

​			&emsp;&emsp;&emsp;1.得到目标方法的拦截器链（增强器包装成拦截器MethodInterceptor）

​			&emsp;&emsp;&emsp;2.利用拦截器链式机制，依次进入每一个拦截器进行执行

​			&emsp;&emsp;&emsp;3.效果：

​				&emsp;&emsp;&emsp;正常执行：前置通知--》目标方法--》后置通知--》返回通知

​				&emsp;&emsp;&emsp;出现异常：前置通知--》目标方法--》后置通知--》异常通知

## Spring扩展原理

​	**BeanPostProcessor：**

​		bean后置处理器，bean创建对象初始化前后进行拦截工作的。

​	**BeanFactoryPostProcessor:**

​		beanFactory的后置处理器，在beanFactory标准初始化之后调用，来定制和修改BeanFactory的内容。所有的bean定义**已经**保存加载到beanFactory中，但bean实例还未创建之前执行。

### BeanFactoryPostProcessor原理：

​	1. ioc容器创建对象

​	2. `this.invokeBeanFactoryPostProcessors(beanFactory);`

​		&emsp;如何执行所有的BeanFactoryPostProcessor并执行他们的方法：

​			&emsp;&emsp;1.直接在BeanFactory中找到所有类型是BeanFactoryPostProcessor的组件，根据优先级进行排序，并执行他们的方法。

​			&emsp;&emsp;2.在初始化创建其他组件之前执行。

```java
@Component
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        System.out.println("MyBeanFactoryPostProcessor...");
        int count = configurableListableBeanFactory.getBeanDefinitionCount();
        String[] name = configurableListableBeanFactory.getBeanDefinitionNames();
        System.out.println("当前BeanFactory中有"+count+"个bean");
        System.out.println(Arrays.asList(name));
    }
}
```

### BeanDefinitionRegistryPostProcessor 

​	BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor

​		postProcessBeanDefinitionRegistry();在所有bean定义信息**将要**被加载，bean实例还未创建之前执行。BeanDefinitionRegistry：Bean定义信息的保存中心，BeanFactory就是按照BeanDefinitionRegistry中保存的每一个bean定义信息来创建bean实例的。

​		优先于BeanFactoryPostProcessor执行，利用BeanDefinitionRegistryPostProcessor 给容器中再额外添加一些组件。

​	原理：

​	1.创建ioc容器

​	2.`this.invokeBeanFactoryPostProcessors(beanFactory);`

​	3.从容器中获取所有的BeanDefinitionRegistryPostProcessor组件

​		&emsp;1.依次触发所有的postProcessBeanDefinitionRegistry()方法

​		&emsp;2.再触发postProcessBeanFactory()方法

```java
@Component
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        int count = beanDefinitionRegistry.getBeanDefinitionCount();
        System.out.println("当前BeanFactory中有"+count+"个bean");
        RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(Black.class);
        beanDefinitionRegistry.registerBeanDefinition("hello", rootBeanDefinition);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        int count = configurableListableBeanFactory.getBeanDefinitionCount();
        System.out.println("当前BeanFactory中有"+count+"个bean");
    }
}
```

### ApplicationListener

​	ApplicationListener：监听容器中发布的事件，事件驱动模型开发

​	`public interface ApplicationListener<E extends ApplicationEvent>`：监听ApplicationEvent及其下面的子事件

​	步骤：

​		1.写一个监听器来监听某个事件（ApplicationEvent及其子类）

​			也可以用@EventListener注解（使用EventListenerMethodProcessor处理器来解析方法上的@EventListener）

​			`EventListenerMethodProcessor implements SmartInitializingSingleton`

​			SmartInitializingSingleton原理：

​				&emsp;1.ioc容器创建对象并`refresh()`

​				&emsp;2.`finishBeanFactoryInitialization(beanFactory)`初始化剩下的单实例bean

​					&emsp;&emsp;1.先创建所有的单实例bean`getBean()`

​					&emsp;&emsp;2.获取所有创建好的单实例bean，判断是否是SmartInitializingSingleton类型。如果是调用`afterSingletonsInstantiated()`

​		2.把监听器放在容器中

​		3.容器中有相关事件的发布，就能监听到这个事件

​			ContextClosedEvent：关闭容器

​			ContextRefreshedEvent：刷新容器

​			ContextStoppedEvent：停止容器

​			ContextStartedEvent：打开容器

```
@Component
public class MyApplicationListener implements ApplicationListener<ApplicationEvent> {
    //当容器中发布此事件以后，方法触发
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        System.out.println("收到事件" + applicationEvent);
    }
}
```

​		4.发布一个事件：`context.publishEvent()`

```java
		AnnotationConfigApplicationContext context = new 					AnnotationConfigApplicationContext(ExtConfig.class);

        context.publishEvent(new ApplicationEvent(new String("自定义发布的事件...")) {
            @Override
            public Object getSource() {
                return super.getSource();
            }
        });
```

​	ApplicationListener原理：

​		1.容器创建，`refresh()`

​		2.调用`this.finishRefresh()`，容器刷新完成

​		3.`this.publishEvent((ApplicationEvent)(new ContextRefreshedEvent(this)));`事件发布流程：

​			&emsp;1.`this.getApplicationEventMulticaster()`获取事件多播器

​			&emsp;2.`Multicaster`派发事件：

​				&emsp;&emsp;1.`Iterator var4 = this.getApplicationListeners(event, type).iterator();`获得所有的`ApplicationListeners`

​				&emsp;&emsp;2.如果有`Executor`，可以支持使用`Executor`进行异步派发

​				&emsp;&emsp;3.否则，同步的方式直接执行listener方法，`invokeListener(listener, event)`	，拿到listener，回调`onApplicationEvent`方法			

​	事件多播器：

​		1.容器创建对象，`refresh()`

​		2.`initApplicationEventMulticaster()`初始化ApplicationEventMulticaster

​			先去容器中查找有没有id为applicationEventMulticaster的组件

​				&emsp;1.有，拿到这个组件

`this.applicationEventMulticaster = (ApplicationEventMulticaster)beanFactory.getBean("applicationEventMulticaster", ApplicationEventMulticaster.class);`

​				&emsp;2.没有，创建一个简单的applicationEventMulticaster

```java
this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
            beanFactory.registerSingleton("applicationEventMulticaster", this.applicationEventMulticaster);
```

​	容器中有哪些监听器：

​		1.容器创建对象，`refresh()`

​		2.`this.registerListeners()`

​			从容器中获取所有的监听器，把他们注册到applicationEventMulticaster中

```java
String[] listenerBeanNames = this.getBeanNamesForType(ApplicationListener.class, true, false);
        String[] var7 = listenerBeanNames;
        int var3 = listenerBeanNames.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String listenerBeanName = var7[var4];
            this.getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
        }
```

### Spring容器的refresh()[创建刷新]

​	BeanFactroy在`refresh()`之前调用父类的无参构造器创建

​	1.`prepareRefresh()`刷新前的预处理

​		&emsp;1.`initPropertySources()`初始化属性设置，通过子类自定义个性化属性设置方法

​		&emsp;2.`this.getEnvironment().validateRequiredProperties()`检验属性的是否合法

​		&emsp;3.`this.earlyApplicationEvents = new LinkedHashSet();`保存容器中早期的事件

​	2.`this.obtainFreshBeanFactory()`获取BeanFactory

​		&emsp;1.`this.refreshBeanFactory()`刷新BeanFactory，设置id

​		&emsp;2.`this.getBeanFactory()`返回创建的BeanFactory对象

​		&emsp;3.返回BeanFactory（DefaultListableBeanFactory）

​	3.`this.prepareBeanFactory(beanFactory)`BeanFactory的预准备工作(进行一些设置)

​		&emsp;1.设置BeanFactory的类加载器、表达式解析器...

​		&emsp;2.添加部分BeanPostProcessor（ApplicationContextAwareProcessor）

​		&emsp;3.设置忽略的自动装配的接口，不能通过接口类型自动注入(EnvironmentAware...)

​		&emsp;4.注册可以解析的自动装配（能直接在任何组件自动注入，BeanFactory、ResourceLoader、ApplicationContext、ApplicationEventPublisher）

​		&emsp;5.添加BeanPostProcessor（ApplicationListenerDetector）

​		&emsp;6.给BeanFactory中注册一些能用的组件：environment(ConfigurableEnvironment)、systemProperties(Map<String, Object>)、systemEnvironment(Map<string, Object>) 

​	4.`this.postProcessBeanFactory(beanFactory)`BeanFactory准备工作完成后进行后置处理工作

​		&emsp;1.子类通过重写这个方法来在BeanFactory创建并准备完成以后做进一步的设置

------------------------------以上是BeanFactory的创建及预准备工作----------------------------

​	5.`this.invokeBeanFactoryPostProcessors(beanFactory)`执行BeanFactoryPostProcessor（BeanFactory的后置处理器，在BeanFactory标准初始化之后执行，即前面四步）

​		两个接口：BeanFactoryPostProcessor、BeanDefinitionRegistryPostProcessor

​		&emsp;1.`invokeBeanFactoryPostProcessors()`

​			&emsp;&emsp;1.获取所有的BeanDefinitionRegistryPostProcessor

​			&emsp;&emsp;2.先执行实现了PriorityOrdered优先级接口的BeanDefinitionRegistryPostProcessor的`invokeBeanDefinitionRegistryPostProcessors`方法

​			&emsp;&emsp;3.再执行实现了Order接口的BeanDefinitionRegistryPostProcessor的`invokeBeanDefinitionRegistryPostProcessors`方法

​			&emsp;&emsp;4.最后执行没有实现任何优先级接口的BeanDefinitionRegistryPostProcessor的`invokeBeanDefinitionRegistryPostProcessors`方法

​			&emsp;&emsp;5.执行BeanFactoryPostProcessor的方法，和1、2、3、4类似

​	6.`this.registerBeanPostProcessors(beanFactory)`注册BeanPostProcessor

​		BeanPostProcessor、DestructionAwareBeanPostProcessor、InstantiationBeanPostProcessor、SmartInstantiationBeanPostProcessor、MergedBeanDefinitionPostProcessor（internalPostProcessor)、不同类型的BeanPostProcessor在Bean创建前后的时机是不一样的

​		&emsp;1.获取所有的后置处理器、后置处理器都可以有优先级

​		&emsp;2.先注册实现了PriorityOrder接口的BeanPostProcessor

​		&emsp;3.再注册实现了Order接口的BeanPostProcessor

​		&emsp;4.再注册剩下的BeanPostProcessor

​		&emsp;5.最后注册MergedBeanDefinitionPostProcessor

​		&emsp;6.注册ApplicationListenerDetector：在bean创建完成后检查是否是ApplicationListener

​	7.`this.initMessageSource()`初始化MessageSource组件(做国际化功能、消息绑定、消息解析)

​		&emsp;1.获取BeanFactory

​		&emsp;2.看容器中是否有id为messageSource、类型为MessageSource的组件。如果有赋值给MessageSource，如果没有创建一个DelegatingMessageSource。MessageSource，取出国际化配置文件中的某个key的值。

​		&emsp;3.把创建的MessageSource注册在容器中，之后获取国际化配置文件中的值时，可以自动注入MessageSource。

​	8.`this.initApplicationEventMulticaster()`初始化事件派发器

​		&emsp;1.获取BeanFactory

​		&emsp;2.从BeanFactory中获取applicationEventMulticaster

​		&emsp;3.如果上一步没有配置，创建一个SimpleApplicationEventMulticaster

​		&emsp;4.将创建的applicationEventMulticaster添加到BeanFactory中，以后其他组件直接自动注入

​	9.`this.onRefresh()`留给子容器（子类）

​		&emsp;1.子类重写这个方法，在容器刷新时可以自定义逻辑

​	10.`this.registerListeners()`将项目中所有的ApplicationListener注册到容器中

​		&emsp;1.从容器中拿到所有的ApplicationListener

​		&emsp;2.将每个监听器添加到事件派发器中

​		&emsp;3.派发之前步骤产生的事件

​	11.`this.finishBeanFactoryInitialization(beanFactory)`初始化所有剩下的单实例Bean

​		&emsp;1.获取容器中的所有Bean，依次进行初始化和创建对象

​		` List beanNames = new ArrayList<>(this.beanDefinitionNames);`

​		&emsp;2.遍历beanNames获取bean 的定义信息

​		`RootBanDefinition bd = getMergedLocalBeanDefinition(beanName);`

​		&emsp;3.判断Bean不是抽象的，是单实例的，不是懒加载得

​		`if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit())`

​			&emsp;&emsp;1.判断是否是FactoryBean；是否是实现FactoryBean接口的Bean

​			&emsp;&emsp;2.如果是，利用工厂方法创建对象getObject();

​			&emsp;&emsp;3.不是工厂Bean，利用getBean(beanName)；创建对象

​			&emsp;&emsp;4.所有Bean都利用getBean创建完成以后； 检查所有的Bean是否是SmartInitializingSingleton接口类型的，如果是就执行 afterSingletonsInstantiated()方法

​			&emsp;&emsp;**getBean()过程:**

​			&emsp;&emsp;1.先获取缓存中保存的单实例bean，如果能获取到，说明这Bean之前被创建过（所有创建过的单实例Bean都会被缓存起来）从getSingleton方法中singletonObjects=new ConcurrentHashMap<String,Object>属性中获取到

​			&emsp;&emsp;2.缓存中获取不到，开始Bean的创建对象流程：

​				&emsp;&emsp;&emsp;1.标记当前Bean已经被创建，markBeanAsCreated(beanName);防止多线程bean重复创建

​				&emsp;&emsp;&emsp;2.获取Bean的定义信息

`final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);`

​				&emsp;&emsp;&emsp;3.获取当前Bean依赖的其它Bean；如果有，按照getBean（）把依赖的Bean先创建出来

​				&emsp;&emsp;&emsp;4.启动单实例Bean的创建流程

​			&emsp;&emsp;&emsp;**单实例Bean的创建流程:**

​			&emsp;&emsp;&emsp;`createBean(beanName,mbd,args)`

​				&emsp;&emsp;&emsp;1.Object bean = resolveBeforeInstantiation(beanName, mbdToUse);先让BeanPostProcessor【InstantiationAwareBeanPostProcessor】先拦截尝试返回代理对象。先触发InstantiationAwareBeanPostProcessor接口的postProcessBeforeInstantiation()方法，如果有返回值调用postProcessAfterInstantiation()方法

​				&emsp;&emsp;&emsp;2.如果没有返回bean，调用`doCreateBean()`

​					&emsp;&emsp;&emsp;&emsp;1.利用工厂方法或者对象的构造器等创建bean实例

​					&emsp;&emsp;&emsp;&emsp;`createBeanInstance(beanName, mbd, args);`

​					&emsp;&emsp;&emsp;&emsp;2.`applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);`

​					&emsp;&emsp;&emsp;&emsp;3.为bean的属性赋值`populateBean(beanName, mbd, instanceWrapper)`

​						&emsp;&emsp;&emsp;&emsp;赋值前：

​						&emsp;&emsp;&emsp;&emsp;&emsp;1.拿到InstantiationAwareBeanPostProcessor类型的后置处理器，执行postProcessAfterInstantiation();

​						&emsp;&emsp;&emsp;&emsp;&emsp;2.拿到InstantiationAwareBeanPostProcessor类型的后置处理器，执行postProcessProperties();

​						&emsp;&emsp;&emsp;&emsp;赋值：

​						&emsp;&emsp;&emsp;&emsp;&emsp;1.应用Bean属性的值：为属性赋值，利用反射调用setter方法等

​						&emsp;&emsp;&emsp;&emsp;&emsp;`applyPropertyValues(beanName, mbd, bw, pvs);`

​					&emsp;&emsp;&emsp;&emsp;4.bean初始化：

​					`initializeBean(beanName, exposedObject, mbd);`

​					&emsp;&emsp;&emsp;&emsp;5.注册bean的销毁

​					&emsp;&emsp;&emsp;&emsp;`registerDisposableBeanIfNecessary(beanName, bean, mbd);`

​				&emsp;&emsp;&emsp;3.Bean的实例创建完成，将bean添加到缓存中

​				&emsp;&emsp;&emsp;`addSingleton(beanName, singletonObject);`

​	12.`this.finishRefresh()`完成BeanFactory初始化创建工作；IOC容器就创建完成

​		&emsp;1.`clearResourceCaches()`清理一些资源缓存

​		&emsp;2.`initLifecycleProcessor()`初始化声明周期有关的后置处理器，允许我们写一个LifecycleProcessor的实现类，可以在BeanFactory进行到特定生命周期时进行调用
​ 默认从容器中找是否有LifeCycleProcessor的组件，如果没有，默认会创建一个 new DefaultLifecycleProcessor();然后加入到容器中
​		&emsp;3.`getLifecycleProcessor().onRefresh()`拿到前面定义的生命周期处理器回调onRefresh()方法

​		&emsp;4.`publishEvent(new ContextRefreshedEvent(this))`发布容器刷新完成事件

​		&emsp;5.`LiveBeansView.registerApplicationContext(this);`