package com.github.nenomm.ks.ktable.stockmarket;


import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Profile;

@Profile("stockMarket")
@EnableBinding(StockSink.class)
public class StockMarketStatistics {
    private static final Logger logger = LoggerFactory.getLogger(StockMarketStatistics.class);

    @StreamListener
    public void crunchNumbers(@Input(StockSink.INPUT) KStream<String, StockInfo> stocks) {
        logger.info("Inside stocks listener");

        KGroupedStream grouped = stocks.groupByKey();

        //grouped.aggregate(StockInfo::new, (key, value, aggregate) -> )

    }
}
