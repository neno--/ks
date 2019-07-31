package com.github.nenomm.ks.oldapi.customserde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class KafkaJsonSerializer implements Serializer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaJsonSerializer.class);

    @Override
    public void configure(Map map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, Object o) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsBytes(o);
        } catch (Exception e) {
            logger.error("Error in serialization", e);
        }
        return retVal;
    }

    @Override
    public void close() {

    }
}
