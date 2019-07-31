package com.github.nenomm.ks.oldapi.customserde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class KafkaJsonDeserializer<T> implements Deserializer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaJsonSerializer.class);

    private Class<T> type;

    public KafkaJsonDeserializer(Class type) {
        this.type = type;
    }

    @Override
    public void configure(Map map, boolean b) {

    }

    @Override
    public Object deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        T obj = null;
        try {
            obj = mapper.readValue(bytes, type);
        } catch (Exception e) {
            logger.error("Error in deserialization", e);
        }
        return obj;
    }

    @Override
    public void close() {

    }
}
