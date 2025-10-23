package com.vic.walletservice.Kafka;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * this configures kafka producer for sending event messages to the history service
 */

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic walletEventTopic() {
        return TopicBuilder.name("wallet_event_topic")
                .partitions(3)
                .replicas(1)
                .config("retention.ms", String.valueOf(172800000)) // 2 days
                .config("retention.bytes", "-1") // no size limit
                .build();
    }

}
