package com.xyrfs.bean.bo.log.sys;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统日志bo
 * @author zhangxh
 */
@Data
@Builder
@AllArgsConstructor
public class SysLogBo implements Serializable {

    private static final long serialVersionUID = 3217907220556047829L;

    /**
     * 异常"NG"，正常"OK"
     */
    private String type;

    private String className;

    private String httpMethod;

    private String classMethod;

    private String params;

    private Long execTime;

    private String remark;

    private LocalDateTime createDate;

    private String url;

    private String ip;

    /**
     * session json
     */
    private String session;


    /**
     * 异常信息
     */
    private String exception;

}
