package com.github.nenomm.ks.transformer;

import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class SimpleTransformer implements ValueTransformer<String, String> {
    public static String STORE_NAME = "MY_TEST_STATE_STORE";

    private KeyValueStore<String, Integer> stateStore;
    private ProcessorContext context;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        stateStore = (KeyValueStore) this.context.getStateStore(STORE_NAME);
    }

    @Override
    public String transform(String value) {
        Integer occurences = stateStore.get(value);

        if (occurences == null) {
            occurences = 1;
        } else {
            occurences++;
        }

        stateStore.put(value, occurences);

        return String.format("%s ---> %d", value, occurences);
    }

    /*@Override
    @SuppressWarnings("deprecation")
    public String punctuate(long timestamp) {
        return null;  //no-op null values not forwarded.
    }*/

    @Override
    public void close() {
        //no-op
    }
}
