package com.github.nenomm.ks.oldapi;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.github.nenomm.ks.oldapi.SimpleExample.STOCK_INFO_SOURCE;

@Profile("oldApiSimple")
@Component
public class MockDataProducer {
    private static final Logger logger = LoggerFactory.getLogger(MockDataProducer.class);
    private static String[] SYMBOLS = {"AAA", "BBB", "CCC", "DDD", "EEE"};

    private KafkaProducer producer;
    private Timer timer = new Timer();
    private Callback callback;
    private Random random;

    @PostConstruct
    private void init() {
        logger.info("Initializing the producer");
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "blade1:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "com.github.nenomm.ks.oldapi.customserde.KafkaJsonSerializer");
        properties.put("acks", "1");
        properties.put("retries", "3");

        producer = new KafkaProducer<>(properties);

        callback = (metadata, exception) -> {
            if (exception != null) {
                logger.error("Error during producing stock info!", exception);
            }
        };

        random = new Random();

        logger.info("Producer initialized");
    }

    private void sendToTopic() {
        ProducerRecord<String, StockInfo> record = new ProducerRecord<>(STOCK_INFO_SOURCE, null, nextRandom());
        producer.send(record, callback);
    }

    private StockInfo nextRandom() {
        String symbol = SYMBOLS[random.nextInt(5)];
        long amount = random.nextInt(10) + 1;
        StockInfo.MarketAction marketAction = random.nextBoolean() ? StockInfo.MarketAction.BUY : StockInfo.MarketAction.SELL;
        double price = (random.nextInt(100) + 1) / 10;

        return StockInfo.newBuilder().setSymbol(symbol).setAmount(amount).setMarketAction(marketAction).setPrice(price).build();
    }

    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendToTopic();
            }
        }, 1000, 1000);
    }

    public void stop() {
        timer.cancel();
    }
}
