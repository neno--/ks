package com.github.nenomm.ks.ktable.wordCounter;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;

public interface WordCounterSink {

    String INPUT = "wordCounterSink";

    @Input(INPUT)
    KStream<?, ?> ingestSomethn();
}
