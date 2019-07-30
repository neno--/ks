package com.github.nenomm.ks.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Profile("customProcessorInterface")
@Component
public class TestScheduler {

    private String[] randomWords = new String[]{"foo", "bar", "foobar", "baz", "fox"};
    private Random random = new Random();

    @Autowired
    private TestSource testSource;

    @PostConstruct
    public void execute() {
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                testSource.output1().send(MessageBuilder.withPayload(randomWords[random.nextInt(5)].toUpperCase()).build());
            }
        }, 1000, 100);
    }
}
