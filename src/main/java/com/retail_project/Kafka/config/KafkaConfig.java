package com.retail_project.Kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic orderTopic(){
        return TopicBuilder.name("order.created").build();
    }
    @Bean
    public NewTopic paymentTopic(){
        return TopicBuilder.name("payment.processed").build();
    }
}
