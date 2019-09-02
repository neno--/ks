package com.github.nenomm.ks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring")
public class CommonConfigProperties {

/*    @Value("${spring.cloud.dataflow.stream.name}")
    private String streamName;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.cloud.dataflow.stream.app.label}")
    private String appLabel;

    @Value("${spring.kafka.bootstrapServers:blade1:9092}")
    private String inputGroup;

    @Value("${spring.kafka.bootstrapServers}")
    private String bootstrapServers;*/

    @Value("${spring.cloud.stream.bindings.input.destination}")
    private String inputTopic;

    @Value("${spring.cloud.stream.bindings.output.destination}")
    private String outputTopic;

/*    @Value("${spring.cloud.stream.kafka.binder.minPartitionCount:1}")
    private Integer partitionCount;

    @Value("${spring.cloud.stream.kafka.binder.replicationFactor:1}")
    private Integer replicationFactor;*/

    /*public String getStreamName() {
        return streamName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAppLabel() {
        return appLabel;
    }

    public String getInputGroup() {
        return inputGroup;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }
*/
    public String getInputTopic() {
        return inputTopic;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

  /*  public Integer getPartitionCount() {
        return partitionCount;
    }

    public Integer getReplicationFactor() {
        return replicationFactor;
    }*/
}
