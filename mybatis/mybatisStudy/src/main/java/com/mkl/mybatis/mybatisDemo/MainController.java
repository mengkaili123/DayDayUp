package com.mkl.mybatis.mybatisDemo;

import com.mkl.mybatis.mybatisDemo.User;
import com.mkl.mybatis.mybatisDemo.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Mengkaili
 * @since 2021/5/9
 */
@RestController
@RequestMapping("/")
public class MainController {

    @Autowired
    private UserMapper userMapper;

    @RequestMapping("/")
    public Object index() {
        User userParam = new User();
        userParam.setSchoolName("Sunny School");
        List<User> userList = userMapper.queryUserBySchoolName(userParam);
        for (User user : userList) {
            System.out.println("name:" + user.getName() + "; email:" + user.getEmail());
        }
        return userList;
    }

}
