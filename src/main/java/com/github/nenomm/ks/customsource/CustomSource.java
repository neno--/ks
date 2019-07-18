package com.github.nenomm.ks.customsource;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface CustomSource {

    String OUTPUT = "customSource";

    @Output(CustomSource.OUTPUT)
    MessageChannel output();

}
