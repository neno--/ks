package com.github.nenomm.ks.oldapi.state;

import com.github.nenomm.ks.oldapi.MockDataProducer;
import com.github.nenomm.ks.oldapi.StockInfo;
import com.github.nenomm.ks.oldapi.customserde.KafkaJsonDeserializer;
import com.github.nenomm.ks.oldapi.customserde.KafkaJsonSerializer;
import com.github.nenomm.ks.oldapi.customserde.StockPerformanceSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

import static org.apache.kafka.streams.Topology.AutoOffsetReset.LATEST;

// /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic stock-info-source --property print.key=true --property print.value=true
// /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic STATEFUL_PROC_SINK_TOPIC --property print.key=true --property print.value=true
// /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic stock-info-appid-stock-performance-store-changelog --property print.key=true --property print.value=true

@Profile("oldApiStateful")
@Component
public class StatefulExample {
    private static final Logger logger = LoggerFactory.getLogger(StatefulExample.class);
    public static String STOCK_INFO_SOURCE = "stock-info-source";

    @Autowired
    private MockDataProducer mockDataProducer;

    @PostConstruct
    public void main() throws Exception {
        String stocksStateStore = "stock-performance-store";

        KeyValueBytesStoreSupplier storeSupplier = Stores.lruMap(stocksStateStore, 100);
        StoreBuilder<KeyValueStore<String, StockPerformance>> storeBuilder = Stores.keyValueStoreBuilder(storeSupplier, Serdes.String(), new StockPerformanceSerde());

        Deserializer<StockInfo> stockInfoDeserializer = new KafkaJsonDeserializer<>(StockInfo.class);
        Serializer<StockInfo> stockInfoSerializer = new KafkaJsonSerializer();

        Serde<String> stringSerde = Serdes.String();

        Serializer<String> stringSerializer = stringSerde.serializer();
        Deserializer<String> stringDeserializer = stringSerde.deserializer();

        Topology topology = new Topology();

        topology.addSource(LATEST,
                STOCK_INFO_SOURCE,
                new UsePreviousTimeOnInvalidTimestamp(),
                stringDeserializer,
                stockInfoDeserializer,
                STOCK_INFO_SOURCE);

        StatefulOldSchoolProcessor statefulOldSchoolProcessor = new StatefulOldSchoolProcessor(stocksStateStore);

        topology.addProcessor("STATEFUL_OLD_SCHOOL_PROCESSOR", () -> statefulOldSchoolProcessor, STOCK_INFO_SOURCE);
        topology.addStateStore(storeBuilder, "STATEFUL_OLD_SCHOOL_PROCESSOR");

        topology.addSink("STATEFUL_PROC_SINK", "STATEFUL_PROC_SINK_TOPIC", stringSerializer, stockInfoSerializer, "STATEFUL_OLD_SCHOOL_PROCESSOR");

        KafkaStreams kafkaStreams = new KafkaStreams(topology, getProperties());

        mockDataProducer.start();
        logger.info("Starting Stock-Info Application now");

        kafkaStreams.cleanUp();
        kafkaStreams.start();

        Thread.sleep(60000);
        logger.info("Shutting down Stock-Info Application  now");
        kafkaStreams.close();

        mockDataProducer.stop();
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "stock-info-client");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "stock-info-group");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stock-info-appid");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "blade1:9092");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        return props;
    }
}
