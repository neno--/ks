package com.github.nenomm.ks.test;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.support.GenericMessage;

import java.util.Random;

@EnableBinding(TestSource.class)
public class TestProducer {

    private String[] randomWords = new String[]{"foo", "bar", "foobar", "baz", "fox"};
    private Random random = new Random();

    @Bean
    @InboundChannelAdapter(channel = TestSource.OUTPUT, poller = @Poller(fixedDelay = "100"))
    public MessageSource<String> sendTestData() {
        return () -> {
            int idx = random.nextInt(5);
            return new GenericMessage<>(randomWords[idx]);
        };
    }
}