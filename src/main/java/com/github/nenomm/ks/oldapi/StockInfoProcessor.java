package com.github.nenomm.ks.oldapi;

import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.To;


public class StockInfoProcessor extends AbstractProcessor<String, StockInfo> {

    private String buySink;
    private String sellSink;

    public StockInfoProcessor(String buySink, String sellSink) {
        this.buySink = buySink;
        this.sellSink = sellSink;
    }

    @Override
    public void process(String key, StockInfo stockInfo) {

        switch (stockInfo.getMarketAction()) {
            case BUY: {
                context().forward(key, stockInfo, To.child(buySink));
                break;
            }
            case SELL: {
                context().forward(key, stockInfo, To.child(sellSink));
                break;
            }
        }
    }
}
