package com.xyrfs.common.enums;


public enum ResultEnum {
    OK(0, "成功"),
    FAIL(-1, "失败"),
    SYSTEM_ERROR(1, "系统错误"),
    SYSTEM_REPEAT_SUBMIT(13,"重复提交出错"),
    USER_AUTHENTICATION_ERROR(209, "用户认证出错"),
    USER_NO_PERMISSION_ERROR(210, "没有该权限"),
    USER_LOGIN_TIME_OUT_ERROR(211, "登录已失效"),
    USER_SESSION_TIME_OUT_ERROR(212, "session过期")
    ;

    private Integer code;

    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
