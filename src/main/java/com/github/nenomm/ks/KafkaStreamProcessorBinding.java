package com.github.nenomm.ks;

import com.github.nenomm.ks.transformer.SimpleTransformer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.StreamPartitioner;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.messaging.handler.annotation.SendTo;

import static com.github.nenomm.ks.transformer.SimpleTransformer.STORE_NAME;

@EnableBinding(KafkaStreamsProcessor.class)
public class KafkaStreamProcessorBinding {
    private static final Logger logger = LoggerFactory.getLogger(KafkaStreamProcessorBinding.class);

    public static final String INPUT_TOPIC = "input";
    public static final String OUTPUT_TOPIC = "output";
    public static final int WINDOW_SIZE_MS = 1000;

    private static int NUMBER_OF_TEMP_TOPIC_PARTITIONS = 10;

    @Autowired
    private ApplicationContext context;


    @StreamListener(INPUT_TOPIC)
    @SendTo({OUTPUT_TOPIC})
    public KStream<String, String> process(KStream<String, String> input) throws Exception {
        /*return input
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
                .map((key, value) -> new KeyValue<>(value, value))
                .groupByKey(Serialized.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.of(WINDOW_SIZE_MS))
                .count(Materialized.as("WordCounts-1"))
                .toStream()
                .map((key, value) -> new KeyValue<>(null, new WordCount(key.key(), value, new Date(key.window().start()), new Date(key.window().end()))));*/

        logger.info("Processing"); // this will be displayed once, on app startup.

        KStream<String, String> filteredStream = input.filter((key, value) -> value.contains("A"));
        KStream<String, String> lowercasedStream = filteredStream.mapValues((readOnlyKey, value) -> value.toLowerCase());
        KStream<String, String> numberedLowercasedStream = lowercasedStream.mapValues((readOnlyKey, value) -> "1234 " + value.toLowerCase());

        // sending stream to topic
        numberedLowercasedStream.to("numberedStream");

        // forEach action test - this simulated per message listener behaviour
        numberedLowercasedStream.foreach(foreachAction());

        // transform the stream
        KStream<String, String> transformed = doStreamTransformation(filteredStream);

        return lowercasedStream;
    }

    private static ForeachAction<String, String> foreachAction() {
        return (key, value) -> {
            if (value.contains("bar"))
                logger.info("There is one bar!");
        };
    }

    private StreamsBuilder getBuilder() throws Exception {
        StreamsBuilderFactoryBean streamsBuilderFactoryBean = context.getBean("&stream-builder-process", StreamsBuilderFactoryBean.class);
        //KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        return streamsBuilderFactoryBean.getObject();
    }

    private KStream<String, String> doStreamTransformation(KStream<String, String> source) throws Exception {
        // init state
        KeyValueBytesStoreSupplier storeSupplier = Stores.inMemoryKeyValueStore(STORE_NAME);
        StoreBuilder<KeyValueStore<String, Integer>> storeBuilder = Stores.keyValueStoreBuilder(storeSupplier, Serdes.String(), Serdes.Integer());
        getBuilder().addStateStore(storeBuilder);


        // since we are doing stateful transformation, it is important that we receive all the data that shoud be routed to one partition
        KStream<String, String> sourceByStateKey = source.through("temp_topic", Produced.with(Serdes.String(), Serdes.String(), getPartitioner()));


        KStream<String, String> transformed = sourceByStateKey.transformValues(() -> new SimpleTransformer(), STORE_NAME);
        transformed.to("transformedStream");

        return transformed;
    }

    private StreamPartitioner<String, String> getPartitioner() {
        return (topic, key, value, numPartitions) -> value.hashCode() % NUMBER_OF_TEMP_TOPIC_PARTITIONS;
    }
}
