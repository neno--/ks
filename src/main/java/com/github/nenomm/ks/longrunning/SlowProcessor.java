package com.github.nenomm.ks.longrunning;

import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlowProcessor extends AbstractProcessor<String, String> {
    private static final Logger logger = LoggerFactory.getLogger(SlowProcessor.class);
    private static final int PUNCTUATOR_INTERVAL = 1000;

    private KeyValueStore<String, String> keyValueStore;
    private String stateStoreName;
    private int forwardingInterval;

    public SlowProcessor(String stateStoreName, int forwardingInterval) {
        this.stateStoreName = stateStoreName;
        this.forwardingInterval = forwardingInterval;
    }

    @Override
    public void init(ProcessorContext context) {
        logger.info("InitingProcessor");
        super.init(context);
        keyValueStore = (KeyValueStore) context().getStateStore(stateStoreName);

        SlowPunctuator punctuator = new SlowPunctuator(keyValueStore, context, this.forwardingInterval);
        context.schedule(PUNCTUATOR_INTERVAL, PunctuationType.WALL_CLOCK_TIME, punctuator);
    }

    @Override
    public void process(String key, String value) {
        if (key != null) {

            //logger.info("Processor doing some work!");
            String oldValue = keyValueStore.get(key);

            keyValueStore.put(key, value);
        }
    }
}
