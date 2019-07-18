package com.github.nenomm.ks.test;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface TestSource {

    String OUTPUT = "output1";

    @Output(TestSource.OUTPUT)
    MessageChannel output1();

}
