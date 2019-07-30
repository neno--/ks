package com.github.nenomm.ks.ktable;


import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.regex.Pattern;

@Profile("KTable")
@EnableBinding(KTableCustomInput.class)
public class KTableStreamsTester {
    private static final Logger logger = LoggerFactory.getLogger(KTableStreamsTester.class);

    @StreamListener
    public void doSomeKTableInput(@Input(KTableCustomInput.INPUT) KTable<String, String> input) {
        logger.info("Inside ktable listener");

        // this will print something on commit interval
        input.toStream().foreach((key, value) -> {
            logger.info("KTABLE KEY: {}, VALUE: {}", key, value);
        });

        //this will consume all messages, so nothing will go to grouping...
        //input.toStream().to("kTableOutput");

        KGroupedTable<String, String> shareVolume1 = input.groupBy((k, v) -> new KeyValue<>(k, v), Serialized.with(Serdes.String(), Serdes.String()));
        KTable<String, Long> result = shareVolume1.count();


        result.toStream().to("testAggregateOutput", Produced.with(Serdes.String(), Serdes.Long()));
    }
}
