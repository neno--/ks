package com.github.nenomm.ks.longrunning;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.UsePreviousTimeOnInvalidTimestamp;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

import static org.apache.kafka.streams.Topology.AutoOffsetReset.LATEST;

@Profile("longRunning")
@Component
public class LongRunningExample {
    private static final Logger logger = LoggerFactory.getLogger(LongRunningExample.class);

    @Value("${spring.cloud.stream.bindings.input.destination}")
    private String inputTopic;

    @Value("${spring.cloud.stream.bindings.output.destination}")
    private String outputTopic;

    @Value("${app.executionTime:60}")
    private int executionTime;

    @Value("${app.forwardingInterval:10}")
    private int forwardingInterval;

    @PostConstruct
    public void main() throws Exception {
        String stateStoreName = "STATE_STORE_NAME";
        KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore(stateStoreName);
        StoreBuilder<KeyValueStore<String, String>> storeBuilder = Stores.keyValueStoreBuilder(storeSupplier, Serdes.String(), Serdes.String());

        Topology topology = new Topology();

        topology.addSource(LATEST,
                "SOURCE",
                new UsePreviousTimeOnInvalidTimestamp(),
                Serdes.String().deserializer(),
                Serdes.String().deserializer(),
                inputTopic);

        SlowProcessor slowProcessor = new SlowProcessor(stateStoreName, forwardingInterval);
        topology.addProcessor("PROCESSOR", () -> slowProcessor, "SOURCE");
        topology.addStateStore(storeBuilder, "PROCESSOR");
        topology.addSink("SINK", outputTopic, Serdes.String().serializer(), Serdes.String().serializer(), "PROCESSOR");

        KafkaStreams kafkaStreams = new KafkaStreams(topology, getProperties());
        kafkaStreams.start();
        logger.info("inputTopic: {}", inputTopic);
        logger.info("outputTopic: {}", outputTopic);
        logger.info("executionTime: {}", executionTime);
        logger.info("forwardingInterval: {}", forwardingInterval);
        logger.info("kstream started");

        Thread.sleep(executionTime * 1000);
        kafkaStreams.close();
        logger.info("kstream stopped");
        logger.info("Shutting down Long-Running Application  now");
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "long-running-client");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "long-running-group");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "long-running-appid");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "blade1:9092");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);

        // exactly once delivery
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once");
        return props;
    }
}