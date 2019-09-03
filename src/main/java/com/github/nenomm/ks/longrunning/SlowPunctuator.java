package com.github.nenomm.ks.longrunning;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlowPunctuator implements Punctuator {
    private static final Logger logger = LoggerFactory.getLogger(SlowPunctuator.class);

    private KeyValueStore<String, String> keyValueStore;
    private ProcessorContext context;
    private int counter = 0;
    private int forwardingInterval;

    public SlowPunctuator(KeyValueStore<String, String> keyValueStore, ProcessorContext context, int forwardingInterval) {
        this.keyValueStore = keyValueStore;
        this.context = context;
        this.forwardingInterval = forwardingInterval;
    }

    @Override
    public void punctuate(long timestamp) {
        counter++;

        if ((counter % this.forwardingInterval) == 0) {
            logger.info("Executing punctuator step {} - FORWARDING", counter);
            logger.info("State store has {} items", keyValueStore.approximateNumEntries());

            KeyValueIterator<String, String> performanceIterator = keyValueStore.all();

            while (performanceIterator.hasNext()) {
                KeyValue<String, String> keyValue = performanceIterator.next();
                String key = keyValue.key;
                String value = keyValue.value;
                context.forward(key, value);
            }
            logger.info("Executing punctuator step {} - FORWARDING DONE", counter);

            counter = 0;
        } else {
            logger.info("Executing punctuator step {} - no work", counter);
            logger.info("State store has {} items", keyValueStore.approximateNumEntries());
        }
    }
}
