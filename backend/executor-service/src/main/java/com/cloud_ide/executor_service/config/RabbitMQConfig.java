package com.cloud_ide.executor_service.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
//    // --- Exchange ---
//    public static final String EXECUTOR_EXCHANGE = "cloudide.executor.exchange";
//
//    // --- Queue ---
//    public static final String JOB_QUEUE = "cloudide.executor.jobs.queue";
//
//    // --- Routing Key ---
//    public static final String JOB_ROUTING_KEY = "executor.job";
//
//    // ========== Exchange Bean ==========
//    @Bean
//    public TopicExchange executorExchange() {
//        return new TopicExchange(EXECUTOR_EXCHANGE, true, false);
//    }
//
//    // ========== Queue Bean ==========
//    @Bean
//    public Queue jobQueue() {
//        return new Queue(JOB_QUEUE, true, false, false);
//    }
//
//    // ========== Binding Bean ==========
//    @Bean
//    public Binding bindJobQueue(Queue jobQueue, TopicExchange executorExchange) {
//        return BindingBuilder.bind(jobQueue)
//                .to(executorExchange)
//                .with(JOB_ROUTING_KEY);
//    }
//
//    // ========== JSON Converter ==========
//    @Bean
//    public Jackson2JsonMessageConverter messageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//
//    // ========== Template ==========
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate template = new RabbitTemplate(connectionFactory);
//        template.setMessageConverter(messageConverter());
//        return template;
//    }
}
