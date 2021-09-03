package com.xyrfs.bean.entity.quartz;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.xyrfs.bean.entity.base.entity.v1.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Tolerate;

/**
 * <p>
 * 任务主表
 * </p>
 *
 * @author zxh
 * @since 2019-10-14
 */
@Data
@TableName("s_job")
@Builder
@EqualsAndHashCode(callSuper=false)
public class SJobEntity extends BaseEntity<SJobEntity> implements Serializable {

    private static final long serialVersionUID = -1825371696489602370L;

    @Tolerate
    public SJobEntity(){}

    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    @TableField("job_name")
    private String job_name;

    /**
     * 任务组编号
     */
    @TableField("job_group_code")
    private String job_group_code;

    /**
     * 任务组名称
     */
    @TableField("job_group_name")
    private String job_group_name;

    /**
     * 关联编号
     */
    @TableField("job_serial_id")
    private Long job_serial_id;

    /**
     * 关联表名字
     */
    @TableField("job_serial_type")
    private String job_serial_type;

    /**
     * 任务说明
     */
    @TableField("job_desc")
    private String job_desc;

    /**
     * 任务简称
     */
    @TableField("job_simple_name")
    private String job_simple_name;

    /**
     * Bean名称
     */
    @TableField("bean_name")
    private String bean_name;

    /**
     * 方法名称
     */
    @TableField("method_name")
    private String method_name;

    /**
     * 参数
     */
    @TableField("params")
    private String params;

    /**
     * 表达式
     */
    @TableField("cron_expression")
    private String cron_expression;

    /**
     * 计划策略：0=默认,1=立即触发执行,2=触发一次执行,3=不触发立即执行
     */
    @TableField("misfire_policy")
    private String misfire_policy;

    /**
     * 是否并发执行（0允许 1禁止）
     */
    @TableField("concurrent")
    private Boolean concurrent;

    /**
     * 判断是否是cron表达式，还是simpletrigger
     */
    @TableField("is_cron")
    private Boolean is_cron;

    /**
     * 是否是已经删除
     */
    @TableField("is_del")
    private Boolean is_del;

    /**
     * 是否有效
     */
    @TableField("is_effected")
    private Boolean is_effected;

    /**
     * 下次运行时间
     */
    @TableField("next_run_time")
    private LocalDateTime next_run_time;

    /**
     * 上次运行时间
     */
    @TableField("last_run_time")
    private LocalDateTime last_run_time;

    /**
     * 起始有效时间
     */
    @TableField("begin_date")
    private LocalDateTime begin_date;

    /**
     * 结束有效时间
     */
    @TableField("end_date")
    private LocalDateTime end_date;

    /**
     * 运行次数
     */
    @TableField("run_times")
    private Integer run_times;

    /**
     * 执行情况
     */
    @TableField("msg")
    private String msg;

    @TableField(value="c_id", fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Long c_id;

    @TableField(value="c_time", fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NOT_EMPTY)
    private LocalDateTime c_time;

    @TableField(value="u_id", fill = FieldFill.INSERT_UPDATE)
    private Long u_id;

    @TableField(value="u_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime u_time;


}
