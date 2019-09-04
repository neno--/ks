package com.github.nenomm.ks.consul;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Profile("consul")
//@EnableAutoConfiguration
//@RefreshScope
@Component
public class OtherBean {
    private static final Logger logger = LoggerFactory.getLogger(OtherBean.class);

    @Value("${fromConsul}")
    private String fromConsul;

    @PostConstruct
    public void post() {
        logger.info("fromConsul: {}", fromConsul);
    }
}
