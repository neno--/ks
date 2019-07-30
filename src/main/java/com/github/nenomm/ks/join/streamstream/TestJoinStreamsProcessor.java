package com.github.nenomm.ks.join.streamstream;


import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Profile;

@Profile("joinStreams")
@EnableBinding({CustomInput.class, AnotherCustomInput.class})
public class TestJoinStreamsProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TestJoinStreamsProcessor.class);

    @StreamListener
    public void doSomeInput(@Input(CustomInput.INPUT) KStream<String, String> input, @Input(AnotherCustomInput.INPUT) KStream<String, String> input2) throws Exception {
        logger.info("Registered streamListener");

        input.foreach((key, value) -> {
            logger.info("from first stream: key {}, value {}", key, value);
        });

        input2.foreach((key, value) -> {
            logger.info("from second stream: key {}, value {}", key, value);
        });

        ValueJoiner<String, String, String> joiner = new SimpleTestJoiner();

        JoinWindows thirtySecsWindow = JoinWindows.of(30 * 1000);

        KStream<String, String> joinedKStream = input.join(input2, joiner, thirtySecsWindow, Joined.with(Serdes.String(), Serdes.String(), Serdes.String()));

        //joinedKStream.print(Printed.toSysOut());
        joinedKStream.to("joinedStream");
    }
}
