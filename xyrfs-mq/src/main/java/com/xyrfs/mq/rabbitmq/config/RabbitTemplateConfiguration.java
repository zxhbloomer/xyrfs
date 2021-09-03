package com.xyrfs.mq.rabbitmq.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitTemplate配置类
 *
 * @author zxh
 * @date 2019年 10月19日 15:28:14
 */
@Configuration
public class RabbitTemplateConfiguration {

    @Bean("fs_RabbitTemplate")
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }
}
