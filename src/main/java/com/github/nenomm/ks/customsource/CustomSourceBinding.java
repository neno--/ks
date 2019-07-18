package com.github.nenomm.ks.customsource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;

@EnableBinding(CustomSource.class)
public class CustomSourceBinding {
    private static final Logger logger = LoggerFactory.getLogger(CustomSourceBinding.class);
}
