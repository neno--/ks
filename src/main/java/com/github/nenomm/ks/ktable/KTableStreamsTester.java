package com.github.nenomm.ks.ktable;


import org.apache.kafka.streams.kstream.KTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;

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
    }
}
