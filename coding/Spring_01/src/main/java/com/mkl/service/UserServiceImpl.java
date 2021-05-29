package com.mkl.service;


import com.spring.*;

/**
 * @author Mengkaili
 * @since 2021/4/26
 */
@Component("userService")
@scope("prototype")
public class UserServiceImpl implements UserService {

    @Autowired
    private OrderService orderService;

    private String name;

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public void test() {
        System.out.println(orderService);
        System.out.println(name);
    }
}
