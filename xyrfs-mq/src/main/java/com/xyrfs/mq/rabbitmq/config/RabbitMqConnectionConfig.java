//package com.xyrfs.mq.rabbitmq.config;
//
//import org.springframework.amqp.core.AcknowledgeMode;
//import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
//import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitAdmin;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.context.annotation.Bean;
//
///**
// * @Author：
// * @Description: 当生产者 使用的Jackson2JsonMessageConverter 序列化 必须配置该类 消费者才可以反序列化
// * @Date：Created
// */
////@Configuration
////@EnableRabbit
//@Deprecated
//public class RabbitMqConnectionConfig {
//
//    @Bean
//    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(CachingConnectionFactory connectionFactory) {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//        factory.setConnectionFactory(connectionFactory);
//        factory.setMessageConverter(new Jackson2JsonMessageConverter());
//        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
//        initQueue(rabbitAdmin);
//        return factory;
//    }
//
//    @Bean("fs_RabbitTemplate")
//    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        return rabbitTemplate;
//    }
//
//    /**
//     * 定义生产者数据队列
//     * @param rabbitAdmin
//     */
//    private void initQueue(RabbitAdmin rabbitAdmin) {
////        rabbitAdmin.declareQueue(new Queue(MQEnum.MQ_OPER_LOG.getQueueCode(),true));
//    }
//
//}