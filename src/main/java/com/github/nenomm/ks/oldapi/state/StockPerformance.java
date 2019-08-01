package com.github.nenomm.ks.oldapi.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class StockPerformance {
    private static final Logger logger = LoggerFactory.getLogger(StockPerformance.class);


    private double averagePrice;
    private boolean inited = false;

    private Instant lastUpdateSent;

    public StockPerformance() {
        // for jackson
    }

    public void updatePriceStats(double newPrice, String symbol) {
        if (!inited) {
            logger.info("init for {}", symbol);
            averagePrice = newPrice;
            inited = true;
        } else {
            averagePrice = (averagePrice + newPrice) / 2;
        }
    }

    public void setLastUpdateSent(Instant lastUpdateSent) {
        this.lastUpdateSent = lastUpdateSent;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public boolean isInited() {
        return inited;
    }
}
