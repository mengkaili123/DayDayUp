package com.mkl.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

/**
 * @author Mengkaili
 * @since 2021/5/28
 */
@Component("MklBeanPostProcessor")
public class MklBeanPostProcessor implements BeanPostProcessor {


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前");
        if (beanName.equals("userService")) {
            ((UserService)bean).setName("lalala");
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后");
        return bean;
    }
}
