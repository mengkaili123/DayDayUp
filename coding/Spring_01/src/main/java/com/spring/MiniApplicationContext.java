package com.spring;


import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mengkaili
 * @since 2021/4/26
 */
public class MiniApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String, Object> singleObjects = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    public MiniApplicationContext(Class configClass) {
        this.configClass = configClass;

        //解析配置类
        //ComponentScan注解，扫描路径，扫描，BeanDefinition，BeanDefinitionMap
        scan(configClass);
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanDefinition);
                singleObjects.put(beanName, bean);
            }
        }

    }

    //创建Bean
    public Object createBean(BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // 依赖注入
            for (Field declaredField : clazz.getDeclaredFields()) {
                // 获取类中的所有属性，并对有Autowired注解的属性进行赋值
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }

            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scan(Class configClass) {
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();
        path = path.replace(".", "/");

        //扫描
        ClassLoader classLoader = MiniApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);
        File file = new File(resource.getFile());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                String fileName = f.getAbsolutePath();
                String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                className = className.replace("/", ".");

                try {
                    Class<?> clazz = classLoader.loadClass(className);

                    if (clazz.isAnnotationPresent(Component.class)) {

                        //表示当前这个类是一个bean
                        //解析类，判断当前bean是单例bean，还是prototype的bean

                        Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                        String beanName = componentAnnotation.value();

                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setClazz(clazz);

                        if (clazz.isAnnotationPresent(scope.class)) {
                            scope scopeAnnotation = clazz.getDeclaredAnnotation(scope.class);
                            beanDefinition.setScope(scopeAnnotation.value());
                        } else {
                            beanDefinition.setScope("singleton");
                        }

                        beanDefinitionMap.put(beanName, beanDefinition);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                Object o = singleObjects.get(beanName);
                return o;
            } else {
                //创建bean对象
                Object bean = createBean(beanDefinition);
                return bean;
            }
        } else {
            //不存在对应的bean
            throw new NullPointerException();
        }
    }
}
