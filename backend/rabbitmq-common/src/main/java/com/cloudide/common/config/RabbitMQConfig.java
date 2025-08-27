package com.cloudide.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // --- Exchanges ---
    public static final String PROJECT_EXCHANGE = "cloudide.project.exchange";

    // --- Queues ---
    public static final String PROJECT_BOOTSTRAP_QUEUE = "cloudide.project.bootstrap.queue";
    public static final String PROJECT_DELETE_QUEUE = "cloudide.project.delete.queue";
    public static final String PROJECT_READY_QUEUE = "cloudide.project.ready.queue";


    // --- Routing Keys ---
    public static final String PROJECT_BOOTSTRAP_KEY = "project.bootstrap";
    public static final String PROJECT_DELETE_KEY = "project.delete";
    public static final String PROJECT_READY_KEY = "project.ready";


    // ========== Exchange Bean ==========
    @Bean
    public TopicExchange projectExchange() {
        return new TopicExchange(PROJECT_EXCHANGE, true, false);
    }

    // ========== Queue Beans ==========
    @Bean
    public Queue projectBootstrapQueue() {
        return new Queue(RabbitMQConfig.PROJECT_BOOTSTRAP_QUEUE, true, false, false);
    }

    @Bean
    public Queue projectDeleteQueue() {
        return new Queue(RabbitMQConfig.PROJECT_DELETE_QUEUE, true, false, false);
    }

    @Bean
    public Queue projectReadyQueue() {
        return new Queue(PROJECT_READY_QUEUE, true, false, false);
    }

    // ========== Binding Beans ==========
    @Bean
    public Binding bindProjectBootstrapQueue(
            @Qualifier("projectBootstrapQueue") Queue projectBootstrapQueue,
            TopicExchange projectExchange) {
        return BindingBuilder.bind(projectBootstrapQueue)
                .to(projectExchange)
                .with(PROJECT_BOOTSTRAP_KEY);
    }

    @Bean
    public Binding bindProjectDeleteQueue(
            @Qualifier("projectDeleteQueue") Queue projectDeleteQueue,
            TopicExchange projectExchange) {
        return BindingBuilder.bind(projectDeleteQueue)
                .to(projectExchange)
                .with(PROJECT_DELETE_KEY);
    }

    @Bean
    public Binding bindProjectReadyQueue(
            @Qualifier("projectReadyQueue") Queue projectReadyQueue,
            TopicExchange projectExchange) {
        return BindingBuilder.bind(projectReadyQueue)
                .to(projectExchange)
                .with(PROJECT_READY_KEY);
    }


    // ========== JSON Converter ==========
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {

        return new Jackson2JsonMessageConverter();
    }

    // ========== Template ==========
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
