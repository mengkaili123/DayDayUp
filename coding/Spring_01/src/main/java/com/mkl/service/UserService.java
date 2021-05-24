package com.mkl.service;


import com.spring.Autowired;
import com.spring.Component;
import com.spring.scope;

/**
 * @author Mengkaili
 * @since 2021/4/26
 */
@Component("userService")
@scope("prototype")
public class UserService {

    @Autowired
    private OrderService orderService;

    public void test() {
        System.out.println(orderService);
    }

}