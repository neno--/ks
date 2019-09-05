package com.github.nenomm.ks.longrunning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.SocketUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Profile("longRunning")
@Component
public class AppContainerCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    private static final Logger logger = LoggerFactory.getLogger(AppContainerCustomizer.class);
    private static byte[] INET_ADDRESS = new byte[]{0, 0, 0, 0};

    @Value("${app.minPort}")
    private int minPort;

    @Value("${app.maxPort}")
    private int maxPort;

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        int httpPort = SocketUtils.findAvailableTcpPort(minPort, maxPort);
        logger.info("Using HTTP port: {}", httpPort);

        try {
            factory.setAddress(InetAddress.getByAddress(INET_ADDRESS));
        } catch (UnknownHostException e) {
            logger.error("Cannot bind to {}, binding to loopback address", INET_ADDRESS);
            factory.setAddress(InetAddress.getLoopbackAddress());
        }
        factory.setPort(httpPort);
    }
}