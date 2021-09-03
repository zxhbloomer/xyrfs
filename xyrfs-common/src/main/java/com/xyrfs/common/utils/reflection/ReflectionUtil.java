package com.xyrfs.common.utils.reflection;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @ClassName: ReflectionUtil
 * @Description: 反射类
 * @Author: zxh
 * @date: 2019/10/15
 * @Version: 1.0
 */
public class ReflectionUtil {


    /**
     * 获取对象的字段的值
     * @param target
     * @param fieldName
     * @return
     */
    public static Object getFieldObject(Object target, String fieldName) {
        Field field = ReflectionUtils.findField(target.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, target);
    }

    /**
     * 获取对象的字段的值
     * @param target
     * @param fieldName
     * @return
     */
    public static <T> T getFieldValue(Object target, String fieldName) {
        Field field = ReflectionUtils.findField(target.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        return (T) ReflectionUtils.getField(field, target);
    }

    /**
     * 设置对象的字段的值
     * @param target
     * @param fieldName
     * @return
     */
    public static void setFieldValue(Object target, String fieldName, Object value) {
        Field field = ReflectionUtils.findField(target.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, target, value);
    }
}
