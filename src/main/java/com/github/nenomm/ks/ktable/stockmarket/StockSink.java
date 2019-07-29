package com.github.nenomm.ks.ktable.stockmarket;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;

public interface StockSink {

    String INPUT = "stockSink";

    @Input(INPUT)
    KStream<?, ?> ingestSomethn();
}
