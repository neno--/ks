package com.github.nenomm.ks.join.streamstream;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;

public interface CustomInput {

    String INPUT = "customInput";

    @Input(INPUT)
    KStream<?, ?> ingestSomethn();
}
