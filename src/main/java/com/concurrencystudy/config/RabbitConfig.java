package com.concurrencystudy.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // 1. 定义队列名字
    public static final String HOT_QUEUE = "user.hot.queue";
    // 2. 定义交换机名字
    public static final String HOT_EXCHANGE = "user.hot.exchange";
    // 3. 定义路由键（暗号）
    public static final String HOT_ROUTING_KEY = "user.hot.routing.key";

    // 创建队列
    @Bean
    public Queue hotQueue() {
        // durable: true 代表持久化，即使 RabbitMQ 重启，队列也不会丢
        return new Queue(HOT_QUEUE, true);
    }

    // 创建直连交换机（Direct Exchange）
    @Bean
    public DirectExchange hotExchange() {
        return new DirectExchange(HOT_EXCHANGE, true, false);
    }

    // 把队列和交换机绑定在一起，并指定暗号（Routing Key）
    @Bean
    public Binding bindingHot() {
        return BindingBuilder.bind(hotQueue()).to(hotExchange()).with(HOT_ROUTING_KEY);
    }
}