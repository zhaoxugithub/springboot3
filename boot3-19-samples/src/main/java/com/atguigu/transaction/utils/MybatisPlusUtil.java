package com.atguigu.transaction.utils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import java.lang.reflect.Field;

/**
 * MybatisPlus工具类
 *
 * @author szl
 * @since 2023-12-28 01:16:09
 */
public class MybatisPlusUtil {
    /**
     * 对传入实体动态拼接查询条件
     *
     * @param target 查询实体
     * @param <T>    实体类型
     * @return 返回查询条件
     */
    public static <T> QueryWrapper<T> queryWrapperBuilder(T target) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        // 获取实体所有属性集合
        Field[] declaredFields = target.getClass()
                                       .getDeclaredFields();
        // 遍历属性集合，获取属型类型和属性值
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            // 获取属性名
            String fieldName = declaredField.getName();

            // 属性值
            Object value = null;
            try {
                // 获取属性值
                value = declaredField.get(target);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            // 获取属性类型
            String simpleName = declaredField.getType()
                                             .getSimpleName();
            // 获取TableField注解对象，用于判断该字段是否在数据库中使用下划线
            TableField tableFieldAnnotation = declaredField.getAnnotation(TableField.class);
            // 如果是String类型，拼接模糊查找条件，否则拼接等值查找，忽略serialVersionUID属性
            if (simpleName.equals("String")) {
                // 若存在tableField注解，则使用注解中的value作为查询列名，若不存在，则使用属性名作为列名
                if (tableFieldAnnotation != null) {
                    String annotationValue = tableFieldAnnotation.value();
                    queryWrapper.like(value != null, annotationValue, value);
                } else {
                    queryWrapper.like(value != null, fieldName, value);
                }

            } else if (!fieldName.equals("serialVersionUID")) {
                // 若存在tableField注解，则使用注解中的value作为查询列名，若不存在，则使用属性名作为列名
                if (tableFieldAnnotation != null) {
                    String annotationValue = tableFieldAnnotation.value();
                    queryWrapper.eq(value != null, annotationValue, value);
                } else {
                    queryWrapper.eq(value != null, fieldName, value);
                }
            }
        }
        return queryWrapper;
    }

    /**
     * @param c   需要获得查询条件的实体对象
     * @param <E> 实体对象类型
     * @return LambdaQueryWrapper
     */
    public static <E> LambdaQueryWrapper<E> getLQWrapper(Class<E> c) {
        return new LambdaQueryWrapper<>();
    }

    /**
     * @param c   需要获得查询条件的实体对象
     * @param <E> 实体对象类型
     * @return QueryWrapper
     */
    public static <E> QueryWrapper<E> getQWrapper(Class<E> c) {
        return new QueryWrapper<>();
    }

    public static <E> LambdaUpdateWrapper<E> lambdaUpdateWrapperBuilder(Class<E> c) {
        return new LambdaUpdateWrapper<>();
    }
}
