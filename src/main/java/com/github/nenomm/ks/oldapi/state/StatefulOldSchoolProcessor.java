package com.github.nenomm.ks.oldapi.state;

import com.github.nenomm.ks.oldapi.StockInfo;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class StatefulOldSchoolProcessor extends AbstractProcessor<String, StockInfo> {
    private static final Logger logger = LoggerFactory.getLogger(StatefulOldSchoolProcessor.class);

    private KeyValueStore<String, StockPerformance> keyValueStore;
    private String stateStoreName;

    public StatefulOldSchoolProcessor(String stateStoreName) {
        this.stateStoreName = stateStoreName;
    }

    @Override
    public void init(ProcessorContext context) {
        super.init(context);
        keyValueStore = (KeyValueStore) context().getStateStore(stateStoreName);

        PunctuatorForOldSchoolProc punctuator = new PunctuatorForOldSchoolProc(keyValueStore, context);
        context.schedule(5000, PunctuationType.WALL_CLOCK_TIME, punctuator);
    }

    @Override
    public void process(String key, StockInfo value) {
        if (key != null) {

            logger.info("Processor doing some work!");

            StockPerformance stockPerformance = keyValueStore.get(key);

            if (stockPerformance == null) {
                stockPerformance = new StockPerformance();
            }

            stockPerformance.updatePriceStats(value.getPrice(), key);
            stockPerformance.setLastUpdateSent(Instant.now());

            keyValueStore.put(key, stockPerformance);
        }
    }
}
