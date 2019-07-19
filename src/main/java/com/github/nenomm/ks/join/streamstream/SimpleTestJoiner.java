package com.github.nenomm.ks.join.streamstream;

import org.apache.kafka.streams.kstream.ValueJoiner;

public class SimpleTestJoiner implements ValueJoiner<String, String, String> {
    @Override
    public String apply(String value1, String value2) {
        return String.format("%s JOINED WITH %s", value1, value2);
    }
}
