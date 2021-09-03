package com.xyrfs.mq.rabbitmq.config;

import com.xyrfs.mq.rabbitmq.enums.MQEnum;
import com.xyrfs.mq.rabbitmq.properties.MQProperties;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQ配置：生产者
 *
 */
@ConditionalOnProperty(prefix = "spring.rabbitmq.custom.producer", name = "has-open", havingValue = "true")
@EnableConfigurationProperties(MQProperties.class)
@Configuration
public class ProducerConfiguration {

    @Autowired
    private MQProperties mqProperties;

    @Bean
    public RabbitAdmin rabbitAdmin(CachingConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Queue queue(RabbitAdmin rabbitAdmin) {
        Queue queue = new Queue(mqProperties.getProducer().getDefaultQueue(), true);
        rabbitAdmin.declareQueue(queue);
        return queue;
    }

    @Bean
    public Exchange exchange(RabbitAdmin rabbitAdmin) {
        Exchange exchange = ExchangeBuilder.topicExchange(mqProperties.getProducer().getDefaultExchange()).durable(false).build();
        rabbitAdmin.declareExchange(exchange);
        return exchange;
    }

    @Bean
    public Binding binding(Queue queue, Exchange exchange, RabbitAdmin rabbitAdmin) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(mqProperties.getProducer().getDefaultRoutingKey()).noargs();
        rabbitAdmin.declareBinding(binding);

        /**
         * 初始化项目中使用的mq
         */
        binding(rabbitAdmin, MQEnum.MQ_TASK_Tenant_ENABLE);
        binding(rabbitAdmin, MQEnum.MQ_TASK_Tenant_Disable);
        return binding;
    }

    /**
     * 设置mq
     *
     * @param rabbitAdmin
     * @param mqEnum
     * @return
     */
    public Binding binding(RabbitAdmin rabbitAdmin, MQEnum mqEnum) {
        /**
         * 设置queue
         */
        Queue queue = new Queue(mqEnum.getQueueCode(), true);
        rabbitAdmin.declareQueue(queue);
        /**
         * 设置exchange
         */
        Exchange exchange = ExchangeBuilder.topicExchange(mqEnum.getExchange()).durable(true).build();
        rabbitAdmin.declareExchange(exchange);
        /**
         * 设置binding
         */
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(mqEnum.getRouting_key()).noargs();
        rabbitAdmin.declareBinding(binding);
        return binding;
    }
}
