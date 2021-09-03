package com.xyrfs.bean.entity.log.mq;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 消息队列日志
 * </p>
 *
 * @author zxh
 * @since 2019-10-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("s_log_mq")
public class SLogMqEntity implements Serializable {

    private static final long serialVersionUID = 4850292309675522117L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * mq的queue编号
     */
    @TableField("code")
    private String code;

    /**
     * mq的queue名称
     */
    @TableField("name")
    private String name;

    /**
     * mq的queue所对应的exchange名称
     */
    @TableField("exchange")
    private String exchange;

    /**
     * mq的queue所对应的routing_key名称
     */
    @TableField("routing_key")
    private String routing_key;

    /**
     * mq的消息体
     */
    @TableField("mq_data")
    private String mq_data;

    @TableField("construct_id")
    private String construct_id;

    /**
     * 发送情况(0：未发送（false）、1：已发送（true）)
     */
    @TableField("producer_status")
    private Boolean producer_status;

    /**
     * 执行情况(0：未接受（false）、1：已接受（true）)
     */
    @TableField("consumer_status")
    private Boolean consumer_status;

    @TableField(value="c_id", fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Long c_id;

    @TableField(value="c_time", fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NOT_EMPTY)
    private LocalDateTime c_time;

    @TableField(value="u_id", fill = FieldFill.INSERT_UPDATE)
    private Long u_id;

    @TableField(value="u_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime u_time;

    /**
     * 数据版本，乐观锁使用
     */
    @Version
    @TableField(value="dbversion", fill = FieldFill.INSERT_UPDATE)
    private Integer dbversion;


}
