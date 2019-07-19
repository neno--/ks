package com.github.nenomm.ks.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@EnableBinding(TestSink.class)
public class TestConsumer {
    private static final Logger logger = LoggerFactory.getLogger(TestConsumer.class);

    @StreamListener(TestSink.INPUT)
    public void receive(String data) {
        //logger.info("Listening: {}", data);
    }
}