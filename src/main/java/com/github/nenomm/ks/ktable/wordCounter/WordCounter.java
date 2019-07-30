package com.github.nenomm.ks.ktable.wordCounter;


import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.regex.Pattern;

@Profile("wordCount")
@EnableBinding(WordCounterSink.class)
public class WordCounter {
    private static final Logger logger = LoggerFactory.getLogger(WordCounter.class);

    @StreamListener(WordCounterSink.INPUT)
    public void countWords(KStream<String, String> words) {
        logger.info("Inside word count listener");

        final Pattern pattern = Pattern.compile("\\W+");

        KStream flatten = words.flatMapValues(value ->
                Arrays.asList(pattern.split(value.toLowerCase())));

        KStream mapped = flatten.map((key, value) -> new KeyValue<Object, Object>(value, value));

        KStream filtered = mapped.filter((key, value) -> (!value.equals("the")));

        KTable countsLong = filtered.groupByKey().count();

        KTable countsString = countsLong.mapValues((readOnlyKey, value) -> Long.toString((Long) value));

        KStream countsStream = countsString.toStream();

        countsStream.to("wordcount-output");
    }
}
