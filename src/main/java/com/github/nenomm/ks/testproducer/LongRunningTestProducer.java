package com.github.nenomm.ks.testproducer;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.Random;

@Profile("longRunningTestProducer")
@Component
public class LongRunningTestProducer {
    private static final Logger logger = LoggerFactory.getLogger(LongRunningTestProducer.class);

    private KafkaProducer producer;
    private Callback callback;

    private int NUM_OF_KEYS = 1000;
    private Random random = new Random();

    private int index = -1;

    @Value("${spring.cloud.stream.bindings.output.destination}")
    private String outputTopic;

    public void sendToTopic() {
        index++;
        index = index % NUM_OF_KEYS;

        String key = Integer.toString(index);
        String value = Integer.toString(random.nextInt());

        ProducerRecord<String, String> record = new ProducerRecord<String, String>(outputTopic, key, value);
        producer.send(record, callback);
    }

    public void init() {
        logger.info("Initializing the producer to outputTopic: {}", outputTopic);

        Properties properties = new Properties();
        properties.put("bootstrap.servers", "blade1:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("acks", "1");
        properties.put("retries", "3");

        producer = new KafkaProducer<>(properties);

        callback = (metadata, exception) -> {
            if (exception != null) {
                logger.error("Error during producing message!", exception);
            }
        };
    }


}