package com.github.nenomm.ks.testproducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Profile("longRunningTestProducer")
@Component
public class LongRunningTestProducerExecutor {
    private static final Logger logger = LoggerFactory.getLogger(LongRunningTestProducerExecutor.class);

    @Autowired
    LongRunningTestProducer producer;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Boolean running = true;

    private void start() {
        executor.execute(() -> {
            while (running) {
                //logger.info("producing message");
                producer.sendToTopic();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        });
    }

    private void stop() {
        running = false;
        executor.shutdown();
    }

    @PostConstruct
    public void execute() throws InterruptedException {
        producer.init();
        start();
        logger.info("started");
        Thread.sleep(5 * 1000);
        stop();
        logger.info("stopped");
    }
}
