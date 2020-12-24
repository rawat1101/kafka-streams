package com.example.kafkaStream;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {
	@Autowired
	private KafkaConfigParam parm;

	private Map<String, Object> getProducerConfig() {
		Map<String, Object> config = new HashMap<>();
		String jaasTemplate = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";";
		String jaasCfg = String.format(jaasTemplate, parm.getUser(), parm.getPassword());

		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, parm.getServers());
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.RETRIES_CONFIG, parm.getRetries());
		config.put(ProducerConfig.ACKS_CONFIG, parm.getAcks());
		config.put("security.protocol", parm.getProtocol());
		config.put("sasl.mechanism", parm.getMechanism());
		config.put("sasl.jaas.config", jaasCfg);
		return config;
	}

	public ProducerFactory<String, Object> producerFactory() {
		Map<String, Object> config = new HashMap<>();
		config = getProducerConfig();
		return new DefaultKafkaProducerFactory<>(config);
	}

	@Bean
	@Scope(scopeName =ConfigurableBeanFactory.SCOPE_PROTOTYPE )
	public KafkaTemplate<String, Object> kafkaPushTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}
}