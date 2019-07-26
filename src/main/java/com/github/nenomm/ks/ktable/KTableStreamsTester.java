package com.github.nenomm.ks.ktable;


import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KGroupedTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Serialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;

import java.util.Arrays;
import java.util.regex.Pattern;

@EnableBinding(KTableCustomInput.class)
public class KTableStreamsTester {
    private static final Logger logger = LoggerFactory.getLogger(KTableStreamsTester.class);

    @StreamListener
    public void doSomeKTableInput(@Input(KTableCustomInput.INPUT) KTable<String, String> input) {
        logger.info("Inside ktable listener");

        // this will print something on commit interval
        /*input.toStream().foreach((key, value) -> {
            logger.info("KTABLE KEY: {}, VALUE: {}", key, value);
        });*/

        KGroupedTable<String, String> shareVolume1 = input.groupBy((k, v) -> new KeyValue<>(k, v), Serialized.with(Serdes.String(), Serdes.String()));
        KTable<String, Long> result = shareVolume1.count();

        result.toStream().through("aggregateOutput");
    }

    private void blah() {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> source = builder.stream("wordcount-input");
        final Pattern pattern = Pattern.compile("\\W+");

        KStream flatten = source.flatMapValues(value ->
                Arrays.asList(pattern.split(value.toLowerCase())));

        KStream mapped = flatten.map((key, value) -> new KeyValue<Object,
                Object>(value, value));

        KStream filtered = mapped.filter((key, value) -> (!value.equals("the")));

        KTable countsLong = filtered.groupByKey().count();

        KTable countsString = countsLong.mapValues((readOnlyKey, value) -> Long.toString((Long) value));

        KStream countsStream = countsString.toStream();

        countsStream.to("wordcount-output");
    }
}
