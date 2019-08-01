package com.github.nenomm.ks.oldapi.state;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PunctuatorForOldSchoolProc implements Punctuator {
    private static final Logger logger = LoggerFactory.getLogger(PunctuatorForOldSchoolProc.class);

    private KeyValueStore<String, StockPerformance> keyValueStore;
    private ProcessorContext context;

    public PunctuatorForOldSchoolProc(KeyValueStore<String, StockPerformance> keyValueStore, ProcessorContext context) {
        this.keyValueStore = keyValueStore;
        this.context = context;
    }

    @Override
    public void punctuate(long timestamp) {
        logger.info("Pushing data downstream!");
        logger.info("State store has {} items", keyValueStore.approximateNumEntries());

        KeyValueIterator<String, StockPerformance> performanceIterator = keyValueStore.all();

        while (performanceIterator.hasNext()) {
            KeyValue<String, StockPerformance> keyValue = performanceIterator.next();
            String key = keyValue.key;
            StockPerformance stockPerformance = keyValue.value;

            if (stockPerformance != null) {
                context.forward(key, stockPerformance);
            }

        }
        logger.info("State store has {} items", keyValueStore.approximateNumEntries());
    }
}
