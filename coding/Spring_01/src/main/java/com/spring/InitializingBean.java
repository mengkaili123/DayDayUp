package com.spring;

/**
 * @author Mengkaili
 * @since 2021/5/26
 */
public interface InitializingBean {

    void afterPropertiesSet() throws Exception;

}
