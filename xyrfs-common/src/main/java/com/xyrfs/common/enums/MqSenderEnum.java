package com.xyrfs.common.enums;

/**
 * @ClassName: mq发送枚举类
 * @Description: TODO
 * @Author: zxh
 * @date: 2019/10/16
 * @Version: 1.0
 */
public enum MqSenderEnum {
    NORMAL_MQ("NORMAL_MQ", "通用队列，无回调"),
    MQ_TENANT_ENABLE("MQ_TENANT_ENABLE", "租户启用队列"),
    MQ_TENANT_DISABLE("MQ_TENANT_DISABLE", "租户禁用队列")
    ;

    private String code;

    private String name;

    MqSenderEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName(){
        return name;
    }

    public String getContent() {
        return "mqCode:" + code +";mqName:" +name;
    }
}
