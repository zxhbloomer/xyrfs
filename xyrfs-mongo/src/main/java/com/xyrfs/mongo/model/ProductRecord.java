package com.xyrfs.mongo.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.Map;

/**
 * Copyright (C), 2018, Banyan Network Foundation
 * ProductRecord
 * 系统请求记录
 *
 * @author Kevin Huang
 * @since version
 * 2018年03月20日 20:44:00
 */
@Document(collection = "product_record")
@Data
@EqualsAndHashCode(callSuper=false)
public class ProductRecord extends MongoRecord {
    private static final long serialVersionUID = -5883569854710510646L;
    // 请求报文
    @Field("request")
    private Object request;
    // 返回报文
    @Field("response")
    private Object response;
    // ip
    @Field("ip")
    private String ip;
    // 请求账号
    @Indexed
    @Field("account")
    private String account;
    // 产品
    @Indexed
    @Field("product")
    private String product;
    // 请求报文解密字符串
    @Field("request_body")
    private String requestBody;

    // 返回报文详情
    @Indexed
    @Field("code")
    private String code;
    @Indexed
    @Field("status")
    private String status;
    @Field("message")
    private String message;
    @Field("result")
    private Object result;
    @Indexed
    @Field("customer_id")
    private String customerId;
    @Indexed
    @Field("gid")
    private String gid;
    @Field("configs")
    private Map<String, String> configs;
}
