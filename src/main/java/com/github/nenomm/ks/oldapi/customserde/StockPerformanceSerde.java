package com.github.nenomm.ks.oldapi.customserde;

import com.github.nenomm.ks.oldapi.state.StockPerformance;
import org.apache.kafka.common.serialization.Serdes;

public class StockPerformanceSerde extends Serdes.WrapperSerde<StockPerformance> {
    public StockPerformanceSerde() {
        super(new KafkaJsonSerializer(), new KafkaJsonDeserializer(StockPerformance.class));
    }
}
