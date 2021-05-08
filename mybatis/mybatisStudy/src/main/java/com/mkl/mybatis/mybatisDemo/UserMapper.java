package com.mkl.mybatis.mybatisDemo;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Mengkaili
 * @since 2021/5/9
 */
@Mapper
public interface UserMapper {
    List<User> queryUserBySchoolName(User user);
}
