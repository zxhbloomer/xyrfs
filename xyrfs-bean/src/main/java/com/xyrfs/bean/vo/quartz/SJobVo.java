package com.xyrfs.bean.vo.quartz;

import com.xyrfs.bean.config.base.v1.BaseVo;
import com.xyrfs.bean.vo.common.condition.PageCondition;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 任务主表
 * </p>
 *
 * @author zxh
 * @since 2019-10-14
 */
@Data
@NoArgsConstructor
@ApiModel(value = "定时任务调度", description = "定时任务调度")
@EqualsAndHashCode(callSuper=false)
public class SJobVo extends BaseVo implements Serializable {

    private static final long serialVersionUID = 7769270858273192316L;

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务名称
     */
    private String job_name;

    /**
     * 任务组名
     */
    private String job_group;

    /**
     * 任务编号
     */
    private String job_serial_id;

    /**
     * 任务说明
     */
    private String job_desc;

    /**
     * 任务简称
     */
    private String job_simple_name;

    /**
     * Bean名称
     */
    private String bean_name;

    /**
     * 方法名称
     */
    private String method_name;

    /**
     * 参数
     */
    private String params;

    /**
     * 表达式
     */
    private String cron_expression;

    /**
     * 计划策略：0=默认,1=立即触发执行,2=触发一次执行,3=不触发立即执行
     */
    private String misfire_policy;

    /**
     * 是否并发执行（0允许 1禁止）
     */
    private Boolean concurrent;

    /**
     * 是否是已经删除

     */
    private Boolean isdel;

    /**
     * 是否有效
     */
    private Boolean is_effected;

    /**
     * 下次运行时间
     */
    private LocalDateTime next_run_time;

    /**
     * 上次运行时间
     */
    private LocalDateTime last_run_time;

    /**
     * 运行次数
     */
    private Integer run_times;

    private Boolean status;

    /**
     * 异常信息
     */
    private String error;

    private Long c_id;

    private LocalDateTime c_time;

    private Long u_id;

    private LocalDateTime u_time;

    /**
     * 换页条件
     */
    private PageCondition pageCondition;

}
