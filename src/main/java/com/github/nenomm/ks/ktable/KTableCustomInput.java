package com.github.nenomm.ks.ktable;

import org.apache.kafka.streams.kstream.KTable;
import org.springframework.cloud.stream.annotation.Input;

public interface KTableCustomInput {

    String INPUT = "kTableCustomInput";

    @Input(INPUT)
    KTable<?, ?> ingestSomethn();
}
