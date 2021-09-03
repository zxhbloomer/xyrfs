package com.xyrfs.bean.pojo.mqsender;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.io.Serializable;

/**
 * @ClassName: MqMessagePojo
 * @Description: mq的消息体
 * @Author: zxh
 * @date: 2019/10/16
 * @Version: 1.0
 */
@Data
@Builder
public class MqMessagePojo implements Serializable {

    private static final long serialVersionUID = 7703914218244201636L;

    @Tolerate
    MqMessagePojo(){}

    /**
     * messagebean类
     */
    private String messageBeanClass;

    /**
     * message内容：json
     */
    private String parameterJson;
}
