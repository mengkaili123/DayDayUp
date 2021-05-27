package com.spring;

/**
 * @author Mengkaili
 * @since 2021/5/28
 */
public interface BeanPostProcessor {

    Object postProcessBeforeInitialization(Object bean, String beanName);

    Object postProcessAfterInitialization(Object bean, String beanName);

}
