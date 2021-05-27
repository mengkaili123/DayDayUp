package com.mkl.service;


import com.spring.*;

/**
 * @author Mengkaili
 * @since 2021/4/26
 */
@Component("userService")
@scope("prototype")
public class UserService implements InitializingBean {

    @Autowired
    private OrderService orderService;

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public void test() {
        System.out.println(orderService);
        System.out.println(name);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("初始化");
    }
}
