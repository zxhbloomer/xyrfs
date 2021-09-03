package com.xyrfs.bean.pojo.mqsender.builder;

import com.alibaba.fastjson.JSON;
import com.xyrfs.bean.pojo.mqsender.MqMessagePojo;
import com.xyrfs.bean.pojo.mqsender.MqSenderPojo;
import com.xyrfs.bean.pojo.reflection.CallInfoReflectionPojo;
import com.xyrfs.common.enums.MqSenderEnum;
import com.xyrfs.common.utils.UuidUtil;

/**
 * @ClassName: MqSenderPojoBuilder
 * @Description: TODO
 * @Author: zxh
 * @date: 2019/10/17
 * @Version: 1.0
 */
public class MqSenderPojoBuilder {

    /**
     * 构筑mq的bean
     * @param messageData    消息数据
     * @param mqSenderEnum   消息code，消息名称
     * @param job_name       任务名称
     * @return
     */
    public static MqSenderPojo buildMqSenderPojo(Object messageData, MqSenderEnum mqSenderEnum, String job_name){
        MqSenderPojo mqSenderPojo = MqSenderPojo.builder()
            .mqMessagePojo(
                MqMessagePojo.builder()
                    .messageBeanClass(messageData.getClass().getName())
                    .parameterJson(JSON.toJSONString(messageData))
                    .build()
            )
            .key(UuidUtil.randomUUID())
            .type(mqSenderEnum.getCode().toString())
            .name(mqSenderEnum.getName())
            .callBackInfo(null)
            .job_name(job_name)
            .build();
        return mqSenderPojo;
    }

    /**
     * 构筑mq的bean
     * @param messageData
     * @param callBackClasz
     * @param functionName
     * @param callBackPara
     * @return
     */
    public MqSenderPojo buildMqSenderPojo(Object messageData, String callBackClasz, String functionName, Object callBackPara, MqSenderEnum mqSenderEnum){
        MqSenderPojo mqSenderPojo = MqSenderPojo.builder()
            .mqMessagePojo(
                MqMessagePojo.builder()
                    .messageBeanClass(messageData.getClass().getName())
                    .parameterJson(JSON.toJSONString(messageData))
                    .build()
            )
            .key(UuidUtil.randomUUID())
            .type(mqSenderEnum.getCode().toString())
            .name(mqSenderEnum.getName())
            .callBackInfo(
                CallInfoReflectionPojo.builder()
                    .className(callBackClasz)
                    .functionName(functionName)
                    .parameterBeanClass(callBackPara.getClass().getName())
                    .parameterJson(JSON.toJSONString(callBackPara))
                    .build()
            )
            .build();
        return mqSenderPojo;
    }
}
