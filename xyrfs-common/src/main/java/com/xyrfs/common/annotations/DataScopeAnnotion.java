package com.xyrfs.common.annotations;

import java.lang.annotation.*;

/**
 * 数据权限过滤注解
 * 
 * @author zxh
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScopeAnnotion
{
    /**
     * 表的别名
     */
    public String tableAlias() default "";
}
