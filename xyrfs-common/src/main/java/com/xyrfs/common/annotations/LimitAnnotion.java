package com.xyrfs.common.annotations;

import com.xyrfs.common.enums.LimitTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解，示例见 com.perfect.manager.controller.code.image.SysSmsCodeController
 * @author Administrator
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LimitAnnotion {

    /**
     * 资源名称，用于说明接口功能
     */
    String name() default "";

    /**
     * 资源 key
     */
    String key() default "";

    /**
     * key prefix
     */
    String prefix() default "";

    /**
     * 时间的，单位秒
     */
    int period();

    /**
     * 限制访问次数
     */
    int count();

    /**
     * 限制类型
     */
    LimitTypeEnum limitType() default LimitTypeEnum.CUSTOMER;
}
