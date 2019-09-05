package com.github.nenomm.ks.longrunning;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
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
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.apache.kafka.streams.Topology.AutoOffsetReset.LATEST;

@Profile("longRunning")
@Component
public class LongRunningExample {
    private static final Logger logger = LoggerFactory.getLogger(LongRunningExample.class);
    private static final String UNIQUE_ID_PREFIX = "slow-test-processor";

    @Value("${spring.cloud.stream.bindings.input.destination}")
    private String inputTopic;

    @Value("${spring.cloud.stream.bindings.output.destination}")
    private String outputTopic;

    @Value("${app.forwardingInterval:10}")
    private int forwardingInterval;

    @Value("${app.tag:long-running-tag}")
    private String tag;

    private SlowProcessor slowProcessor;
    private KafkaStreams kafkaStreams;

    @PostConstruct
    public void main() throws Exception {
        String stateStoreName = "STATE_STORE_NAME";
        KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore(stateStoreName);
        StoreBuilder<KeyValueStore<String, String>> storeBuilder = Stores.keyValueStoreBuilder(storeSupplier, Serdes.String(), Serdes.String()).withLoggingEnabled(getChangelogConfig());

        Topology topology = new Topology();

        topology.addSource(LATEST,
                "SOURCE",
                new UsePreviousTimeOnInvalidTimestamp(),
                Serdes.String().deserializer(),
                Serdes.String().deserializer(),
                inputTopic);

        slowProcessor = new SlowProcessor(stateStoreName, forwardingInterval);

        topology.addProcessor("PROCESSOR", () -> {
            logger.info("Reusing processor for topology, reuse number: {}", slowProcessor.getAndIncrement());
            return slowProcessor;
        }, "SOURCE");
        topology.addStateStore(storeBuilder, "PROCESSOR");
        topology.addSink("SINK", outputTopic, Serdes.String().serializer(), Serdes.String().serializer(), "PROCESSOR");

        kafkaStreams = new KafkaStreams(topology, getDefaultStreamProperties(tag));
        kafkaStreams.start();
        logger.info("inputTopic: {}", inputTopic);
        logger.info("outputTopic: {}", outputTopic);
        logger.info("forwardingInterval: {}", forwardingInterval);
        logger.info("tag: {}", tag);
        logger.info("kstream started");
    }

    // old stuff
    private static Properties getProperties(String tag) {
        Properties props = new Properties();
        props.put(StreamsConfig.CLIENT_ID_CONFIG, UNIQUE_ID_PREFIX + tag + "CLIENT_ID");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_ID_PREFIX + tag + "GROUP_ID");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, UNIQUE_ID_PREFIX + tag + "APP_ID");

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

    private static Properties getDefaultStreamProperties(String tag) {
        Properties props = new Properties();
        props.put(StreamsConfig.CLIENT_ID_CONFIG, UNIQUE_ID_PREFIX + tag + "CLIENT_ID");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UNIQUE_ID_PREFIX + tag + "GROUP_ID");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, UNIQUE_ID_PREFIX + tag + "APP_ID");

        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "blade1:9092");

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);

        return getDefaultStreamProperties(props);
    }

    private static Properties getDefaultStreamProperties(Properties defaultStreamProperties) {
        defaultStreamProperties.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        defaultStreamProperties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0L);

        // Set commit interval to 1 second.
        defaultStreamProperties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        defaultStreamProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);
        defaultStreamProperties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        /*
            The maximum delay between invocations of poll() when using
            consumer group management. This places an upper bound on the amount of time that the consumer can be idle
            before fetching more records. If poll() is not called before expiration of this timeout, then the consumer
            is considered failed and the group will rebalance in order to reassign the partitions to another member.
        */

        //defaultStreamProperties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, Integer.valueOf(600000).intValue());
        defaultStreamProperties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, Integer.valueOf(60 * 1000).intValue());

        /*
            public static final String HEARTBEAT_INTERVAL_MS_CONFIG = "heartbeat.interval.ms";
            private static final String HEARTBEAT_INTERVAL_MS_DOC =
            "The expected time between heartbeats to the consumer coordinator when using Kafka's group management facilities.
            Heartbeats are used to ensure that the consumer's session stays active and to
            facilitate rebalancing when new consumers join or leave the group.
            The value must be set lower than <code>session.timeout.ms</code>,
            but typically should be set no higher than 1/3 of that value.
            It can be adjusted even lower to control the expected time for normal rebalances.";
        */

        defaultStreamProperties.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);

        /*
            public static final String SESSION_TIMEOUT_MS_CONFIG = "session.timeout.ms";
            private static final String SESSION_TIMEOUT_MS_DOC =
            "The timeout used to detect consumer failures when using Kafka's group management facility.
            The consumer sends periodic heartbeats to indicate its liveness to the broker.
            If no heartbeats are received by the broker before the expiration of this session timeout,
            then the broker will remove this consumer from the group and initiate a rebalance.
            Note that the value must be in the allowable range as configured in the broker configuration by
            <code>group.min.session.timeout.ms</code> and <code>group.max.session.timeout.ms</code>.";
        */

        defaultStreamProperties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 20000);

        /*
            The maximum amount of time in ms that the transaction coordinator will wait for
            a transaction status update from the producer before proactively aborting the ongoing transaction.
            If this value is larger than the transaction.max.timeout.ms setting in the broker, the request will
            fail with a <code>InvalidTransactionTimeout</code> error.";
        */

        defaultStreamProperties.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 20000);

        /*
            "The maximum amount of time in ms that the transaction coordinator will wait for a transaction status update
            from the producer before proactively aborting the ongoing transaction.
            If this value is larger than the transaction.max.timeout.ms setting in the broker,
            the request will fail with a <code>InvalidTransactionTimeout</code> error.";
        */

        defaultStreamProperties.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 600000);

        /*
            "The configuration controls how long <code>KafkaProducer.send()</code> and <code>KafkaProducer.partitionsFor()</code> will block.
            These methods can be blocked either because the buffer is full or metadata unavailable.Blocking in
            the user-supplied serializers or partitioner will not be counted against this timeout.";
        */

        defaultStreamProperties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 240000);

        defaultStreamProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 350000);

        /*
            "The producer will attempt to batch records together into fewer requests whenever multiple records are being sent to the same partition.
            This helps performance on both the client and the server. This configuration controls the default batch size in bytes. <p>
            No attempt will be made to batch records larger than this size. <p>Requests sent to brokers will contain multiple batches,
            one for each partition with data available to be sent. <p>A small batch size will make batching less common and may reduce throughput (a batch size of zero will disable batching entirely). A very large batch size may use memory a bit more wastefully as we will always allocate a buffer of the specified batch size in anticipation of additional records.";
         */

        //defaultStreamProperties.put(ProducerConfig.BATCH_SIZE_CONFIG,32*1024*1024)

        /*
            The producer groups together any records that arrive in between request transmissions into a single batched request.
            Normally this occurs only under load when records arrive faster than they can be sent out. However in some circumstances the
            client may want to reduce the number of requests even under moderate load. This setting accomplishes this by adding a small amount
            of artificial delay&mdash;that is, rather than immediately sending out a record the producer will wait for up to the given delay
            to allow other records to be sent so that the sends can be batched together. This can be thought of as analogous to Nagle's algorithm in TCP.
            This setting gives the upper bound on the delay for batching: once we get <code>batch.size</code> worth of records for a partition
            it will be sent immediately regardless of this setting, however if we have fewer than this many bytes accumulated for this partition we will 'linger'
            for the specified time waiting for more records to show up. This setting defaults to 0 (i.e. no delay). Setting <code>linger.ms=5</code>, for example,
            would have the effect of reducing the number of requests sent but would add up to 5ms of latency to records sent in the absence of load.";
        */

        // defaultStreamProperties.put(ProducerConfig.LINGER_MS_CONFIG,5000)

        return defaultStreamProperties;
    }

    // todo: add this to statestore
    private static Map<String, String> getChangelogConfig() {
        Map<String, String> changelogConfig = new HashMap<>();

        changelogConfig.put("min.insync.replicas", "1");

        return changelogConfig;
    }

    public void pausePunctuator(int sleepDuration) {
        slowProcessor.getPunctuator().pause(sleepDuration);
    }

    @PreDestroy
    public void closeStream() {
        logger.info("close and clean up kafkaStreams...");
        kafkaStreams.close();
        kafkaStreams.cleanUp();
        logger.info("close and clean up done.");
    }
}