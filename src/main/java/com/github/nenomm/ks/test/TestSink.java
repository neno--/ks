package com.github.nenomm.ks.test;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface TestSink {

    String INPUT = "input1";

    @Input(INPUT)
    SubscribableChannel input1();

}