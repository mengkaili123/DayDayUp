package com.mkl.mybatis.mybatisDemo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mengkaili
 * @since 2021/5/12
 */
public class ReflectionStudy {

    public static void main(String[] args) {
        User oldUser = new User(1, "yee");
        User newUser = new User(1, "yeecode");

        System.out.println("不使用反射，只能比较单一类型的对象：");

        Map<String, String> diffUserMap = diffUser(oldUser, newUser);
        for (Map.Entry<String, String> stringStringEntry : diffUserMap.entrySet()) {
            System.out.println("属性" + stringStringEntry.getKey() + "；变化为：" + stringStringEntry.getValue());
        }

        System.out.println("使用反射，可以比较属性不同的各类对象：");

        Map<String, String> diffObjMap = diffObj(oldUser, newUser);
        for (Map.Entry<String, String> stringStringEntry : diffObjMap.entrySet()) {
            System.out.println("属性" + stringStringEntry.getKey() + "；变化为：" + stringStringEntry.getValue());
        }

        Book oldBook = new Book("语文", 15.7);
        Book newBook = new Book("语文", 18.7);
        diffObjMap = diffObj(oldBook, newBook);
        for (Map.Entry<String, String> stringStringEntry : diffObjMap.entrySet()) {
            System.out.println("属性" + stringStringEntry.getKey() + "；变化为：" + stringStringEntry.getValue());
        }

    }

    public static Map<String, String> diffObj(Object oldObj, Object newObj) {
        Map<String, String> diffMap = new HashMap<>();
        try {
            // 获取对象的类
            Class oldObjClazz = oldObj.getClass();
            Class newObjClazz = newObj.getClass();
            // 判断两个对象是否属于同一个类
            if (oldObjClazz.equals(newObjClazz)) {
                // 获取对象所有属性
                Field[] fields = oldObjClazz.getDeclaredFields();
                // 对每一个属性逐一判断
                for (Field field : fields) {
                    // 使属性可以被反射访问
                    field.setAccessible(true);
                    // 获取当前属性的值
                    Object oldValue = field.get(oldObj);
                    Object newValue = field.get(newObj);
                    // 如果某个属性的值在两个对象中不同，则进行记录
                    if ((oldValue == null && newValue != null) || oldValue != null && !oldValue.equals(newValue)) {
                        diffMap.put(field.getName(), "from" + oldValue + "to" + newValue);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return diffMap;
    }

    public static Map<String, String> diffUser(User oldUser, User newUser) {
        Map<String, String> diffMap = new HashMap<>();
        if ((oldUser.getId() == null && newUser.getId() != null) || (oldUser.getId() != null && !oldUser.getId().equals(newUser.getId()))) {
            diffMap.put("id", "from" + oldUser.getId() + "to" + newUser.getId());
        }
        if ((oldUser.getName() == null && newUser.getName() != null) || (oldUser.getId() != null && !oldUser.getName().equals(newUser.getName()))) {
            diffMap.put("name", "from" + oldUser.getName() + "to" + newUser.getName());
        }
        return diffMap;
    }

}
