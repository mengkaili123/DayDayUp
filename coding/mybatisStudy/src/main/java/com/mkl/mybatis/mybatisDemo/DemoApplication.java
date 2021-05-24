package com.mkl.mybatis.mybatisDemo;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		//MyBatis初始化
		String resource = "mybatis-config.xml";
		InputStream inputStream = null;
		try {
			inputStream = Resources.getResourceAsStream(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SqlSessionFactory sqlSessionFactory =
				new SqlSessionFactoryBuilder().build(inputStream);

//		//数据读写
//		try(SqlSession sqlSession = sqlSessionFactory.openSession()) {
//			// 找到接口对应的实现
//			UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
//			User userParam = new User();
//			userParam.setSchoolName("Sunny School");
//			List<User> userList = userMapper.queryUserBySchoolName(userParam);
//			for (User user : userList) {
//				System.out.println("name:" + user.getName() + "; email:" + user.getEmail());
//			}
//		}
	}

}
