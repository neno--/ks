package com.github.nenomm.ks.oldapi;

public class StockInfo {
    public enum MarketAction {
        BUY,
        SELL
    }

    private String symbol;
    private double price;
    private MarketAction marketAction;
    private long amount;

    private StockInfo() {
        // for jackson
    }

    private StockInfo(Builder builder) {
        symbol = builder.symbol;
        price = builder.price;
        marketAction = builder.marketAction;
        amount = builder.amount;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public MarketAction getMarketAction() {
        return marketAction;
    }

    public long getAmount() {
        return amount;
    }

    public static final class Builder {
        private String symbol;
        private double price;
        private MarketAction marketAction;
        private long amount;

        private Builder() {
        }

        private Builder(StockInfo stockInfo) {
            symbol = stockInfo.symbol;
            price = stockInfo.price;
            marketAction = stockInfo.marketAction;
            amount = stockInfo.amount;
        }

        public Builder setSymbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder setPrice(double price) {
            this.price = price;
            return this;
        }

        public Builder setMarketAction(MarketAction marketAction) {
            this.marketAction = marketAction;
            return this;
        }

        public Builder setAmount(long amount) {
            this.amount = amount;
            return this;
        }

        public StockInfo build() {
            return new StockInfo(this);
        }
    }
}
