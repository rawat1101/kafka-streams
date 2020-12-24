package com.example.kafkaStream;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class CommonConfigs {
	@Bean
	@ConfigurationProperties(prefix = "kstream.kafka")
	public KafkaConfigParam kafkaConfigParam() {
		return new KafkaConfigParam();
	}

}