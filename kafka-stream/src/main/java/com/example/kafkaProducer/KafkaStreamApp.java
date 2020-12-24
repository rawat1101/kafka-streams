package com.example.kafkaProducer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.kafka.annotation.EnableKafka;

import com.example.kafkaStream.KafkaConfigParam;
import com.example.kafkaStream.RocksDBCustomConfig;

@EnableKafka
@SpringBootApplication(scanBasePackages = { "com.example" }, exclude = KafkaAutoConfiguration.class)
public class KafkaStreamApp implements CommandLineRunner {
	@Autowired
	private KafkaConfigParam parm;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(KafkaStreamApp.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.setLogStartupInfo(false);
		app.run(args);
	}

	@Value("${kstream.kafka.topic}")
	String topic;

//	@Override
	public void run0(String... args) throws Exception {
		try {
			String jaasTemplate = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";";
			String jaasCfg = String.format(jaasTemplate, parm.getUser(), parm.getPassword());

			Properties props = new Properties();
			props.put(StreamsConfig.APPLICATION_ID_CONFIG, "oddEven-app");
			props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, parm.getServers());
			props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
			props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
			props.put(StreamsConfig.STATE_DIR_CONFIG, "/home/mahendra/kafka-streams");
			props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, "3");
//			props.put(StreamsConfig.topicPrefix(TopicConfig.SEGMENT_MS_CONFIG), 100);
//			props.put(StreamsConfig.topicPrefix(TopicConfig.CLEANUP_POLICY_CONFIG), "delete");
			props.put(StreamsConfig.topicPrefix(TopicConfig.RETENTION_MS_CONFIG), 600000);
			props.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, RocksDBCustomConfig.class);

			props.put("security.protocol", parm.getProtocol());
			props.put("sasl.mechanism", parm.getMechanism());
			props.put("sasl.jaas.config", jaasCfg);

			StreamsBuilder builder = new StreamsBuilder();
			KStream<String, String> textLines = builder.stream("kStreamIn");

//			textLines.filter((k, v) -> Integer.valueOf(v) % 2 == 0).foreach((k, v) -> System.out.println(v));

			final KGroupedStream<String, String> groupedByKey = textLines.groupBy((key, value) -> key,
					Grouped.with("kStreamPart", Serdes.String(), Serdes.String()));
//					.groupByKey();
			KTable<String, Long> keyCounts =
//					groupedByKey.count();
					groupedByKey.count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("counts-store"));
//			keyCounts.toStream().to("kStreamOut");
			keyCounts.toStream().to("kStreamOut", Produced.with(Serdes.String(), Serdes.Long()));
//			keyCounts.toStream().foreach((k, v) -> System.out.println(k + "  " + v));
			final Topology topology = builder.build();
			System.out.println(topology.describe());
			KafkaStreams streams = new KafkaStreams(topology, props);
			streams.start();
		} catch (Exception e) {
		}
	}

	public void run(String... args) throws Exception {
		try {
			String jaasTemplate = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";";
			String jaasCfg = String.format(jaasTemplate, parm.getUser(), parm.getPassword());

			Properties props = new Properties();
			props.put(StreamsConfig.APPLICATION_ID_CONFIG, "oddEven-app");
			props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, parm.getServers());
			props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
			props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
			props.put(StreamsConfig.STATE_DIR_CONFIG, "/home/mahendra/kafka-streams");
			props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, "3");
//			props.put(StreamsConfig.topicPrefix(TopicConfig.SEGMENT_MS_CONFIG), 100);
//			props.put(StreamsConfig.topicPrefix(TopicConfig.CLEANUP_POLICY_CONFIG), "delete");
			props.put(StreamsConfig.topicPrefix(TopicConfig.RETENTION_MS_CONFIG), 600000);
			props.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, RocksDBCustomConfig.class);

			props.put("security.protocol", parm.getProtocol());
			props.put("sasl.mechanism", parm.getMechanism());
			props.put("sasl.jaas.config", jaasCfg);

			StreamsBuilder builder = new StreamsBuilder();
			KStream<String, String> textLines = builder.stream("kStreamIn");

			KTable<String, List<Integer>> table = textLines.groupBy((k, v) -> k)
					.aggregate(ArrayList::new,
					(key, value, aggregate) -> {
						aggregate.add(aggregate.size() >= 1 ? 2 : 1);
						return aggregate;
					}, Materialized.as("counts-store"));
			table.toStream().foreach((k, v) -> System.out.println(k + "  " + v));

			final Topology topology = builder.build();
			System.out.println(topology.describe());
			KafkaStreams streams = new KafkaStreams(topology, props);
			streams.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*{
		StreamsBuilder builder = new StreamsBuilder();
		KStream<String, String> textLines = builder.stream("kStreamIn");
		KTable<String, List<Integer>> table = textLines.groupBy((k, v) -> k).aggregate(ArrayList::new,
				(key, value, aggregate) -> {
					aggregate.add(aggregate.size() >= 1 ? 2 : 1);
					return aggregate;
				}, Materialized.as("counts-store"));
	}*/
}
