package com.mkl;

import com.mkl.service.UserService;
import com.spring.MiniApplicationContext;

/**
 * @author Mengkaili
 * @since 2021/4/26
 */
public class test {

    public static void main(String[] args) {
        MiniApplicationContext miniApplicationContext = new MiniApplicationContext(AppConfig.class);
//        Object userService = miniApplicationContext.getBean("userService");
//        System.out.println(userService);
        UserService userService1 = (UserService) miniApplicationContext.getBean("userService");
        userService1.test();
    }

}
