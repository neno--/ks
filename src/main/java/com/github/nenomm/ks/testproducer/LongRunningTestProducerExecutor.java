package com.github.nenomm.ks.testproducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Profile("longRunningTestProducer")
@Component
public class LongRunningTestProducerExecutor {
    private static final Logger logger = LoggerFactory.getLogger(LongRunningTestProducerExecutor.class);

    @Autowired
    LongRunningTestProducer producer;

    @Value("${app.messagesPerSec:1}")
    private int messagesPerSec;

    @Value("${app.executionTime:60}")
    private int executionTime;

    private AtomicInteger counter = new AtomicInteger(0);

    private Timer timer = new Timer();

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private AtomicBoolean running = new AtomicBoolean(true);

    private void start() {
        schedule();

        executor.execute(() -> {
            while (running.get()) {
                if (counter.get() < messagesPerSec) {
                    producer.sendToTopic();
                    counter.incrementAndGet();
                }
            }
        });
    }

    private void stop() {
        running.set(false);
        executor.shutdown();
        timer.cancel();
    }

    private void schedule() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                counter.set(0);
                logger.info("Resetting counter to {}", counter.get());
            }
        }, 0, 1000);
    }

    @PostConstruct
    public void execute() throws InterruptedException {
        producer.init();
        start();
        logger.info("app.messagesPerSec: {}", messagesPerSec);
        logger.info("app.executionTime: {}", executionTime);
        logger.info("started");
        Thread.sleep(executionTime * 1000);
        stop();
        logger.info("stopped");
    }
}
