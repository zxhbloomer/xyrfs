package com.xyrfs.bean.pojo.reflection;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.io.Serializable;

/**
 * @ClassName: ReflectionPoJo
 * @Description: 反射bean
 * @Author: zxh
 * @date: 2019/10/15
 * @Version: 1.0
 */
@Data
@Builder
public class CallInfoReflectionPojo implements Serializable {

    private static final long serialVersionUID = -2922324307942065265L;

    @Tolerate
    CallInfoReflectionPojo(){}

    /**
     * 类名:"audit.service.DegreeFunctionTest"
     */
    private String className;

    /**
     * 函数名、方法名："saveDo"
     */
    private String functionName;

    /**
     * 参数类型名："audit.entity.entityTest.TestListBean"
     */
    private String parameterBeanClass;

    /**
     * 参数数据:'[{"x1":"setData","x2":88888888,"x3":true,"x4":[{"x1":"setSubList1","x2":55555555,"x3":true,"x4":[{"x1":"TestSubSubListBean1","x2":111111,"x3":true},{"x1":"TestSubSubListBean2","x2":222222,"x3":true}]},{"x1":"setSubList2","x2":333333,"x3":true,"x4":[{"x1":"TestSubSubListBean1","x2":111111,"x3":true},{"x1":"TestSubSubListBean2","x2":222222,"x3":true}]}]},{"x1":"setData","x2":7777777,"x3":true,"x4":[{"x1":"setSubList1","x2":55555555,"x3":true,"x4":[{"x1":"TestSubSubListBean1","x2":111111,"x3":true},{"x1":"TestSubSubListBean2","x2":222222,"x3":true}]},{"x1":"setSubList2","x2":333333,"x3":true,"x4":[{"x1":"TestSubSubListBean1","x2":111111,"x3":true},{"x1":"TestSubSubListBean2","x2":222222,"x3":true}]}]}]'
     */
    private String parameterJson;
}
