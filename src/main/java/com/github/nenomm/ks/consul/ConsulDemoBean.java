package com.github.nenomm.ks.consul;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Profile("consul")
@Component
public class ConsulDemoBean {
    private static final Logger logger = LoggerFactory.getLogger(ConsulDemoBean.class);

    @Autowired
    private OtherBean otherBean;

    @PostConstruct
    private void main() throws Exception {
        logger.info("Running consul demo");
        Thread.sleep(5 * 60 * 1000);
    }
}
